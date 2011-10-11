package org.review_board.ereviewboard.core;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.eclipse.mylyn.tasks.core.IRepositoryPerson;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.review_board.ereviewboard.core.client.ReviewboardClientData;
import org.review_board.ereviewboard.core.model.User;

/**
 * @author Robert Munteanu
 */
public class ReviewboardAttributeMapper extends TaskAttributeMapper {

    public enum Attribute {

        ID("id", "Id", TaskAttribute.TYPE_INTEGER, null),
        REPOSITORY("repository.title", "Repository", TaskAttribute.TYPE_SHORT_RICH_TEXT, TaskAttribute.KIND_DEFAULT),
        // repository sub-attributes
        REPOSITORY_TOOL("repository.tool", "Repository Tool", TaskAttribute.TYPE_SHORT_TEXT, null),
        REPOSITORY_PATH("repository.path", "Repository Path", TaskAttribute.TYPE_SHORT_TEXT, null),
        REPOSITORY_ID("repository.id", "Repository Id", TaskAttribute.TYPE_SHORT_TEXT, null),
        SUBMITTER("submitter.title", "Submitter", TaskAttribute.TYPE_PERSON, null),
        SUMMARY("summary", "Summary", TaskAttribute.TYPE_SHORT_RICH_TEXT, null),
        DESCRIPTION("description", "Description", TaskAttribute.TYPE_LONG_RICH_TEXT, null),
        TESTING_DONE("testing_done", "Testing done", TaskAttribute.TYPE_LONG_RICH_TEXT, null),
        STATUS("status", "Status", TaskAttribute.TYPE_SHORT_RICH_TEXT, TaskAttribute.KIND_DEFAULT),
        SHIP_IT("ship-it", "Ship-it", TaskAttribute.TYPE_SHORT_RICH_TEXT, TaskAttribute.KIND_DEFAULT),
        BUGS_CLOSED("bugs_closed", "Bugs closed", TaskAttribute.TYPE_SHORT_RICH_TEXT, TaskAttribute.KIND_DEFAULT),
        PUBLIC("public", "Public", TaskAttribute.TYPE_BOOLEAN, TaskAttribute.KIND_DEFAULT),
        BRANCH("branch", "Branch", TaskAttribute.TYPE_SHORT_RICH_TEXT, TaskAttribute.KIND_DEFAULT),
        CHANGENUM("changenum", "Change number", TaskAttribute.TYPE_SHORT_RICH_TEXT, TaskAttribute.KIND_DEFAULT),
        TARGET_PEOPLE("target_people", "People", TaskAttribute.TYPE_PERSON, TaskAttribute.KIND_PEOPLE),
        TARGET_GROUPS("target_groups", "Groups", TaskAttribute.TYPE_PERSON, TaskAttribute.KIND_PEOPLE),
        LAST_UPDATED("last_updated", "Last updated", TaskAttribute.TYPE_DATETIME, null),
        TIME_ADDED("time_added", "Time added", TaskAttribute.TYPE_DATETIME, null),
        OPERATION_STATUS("operation_status", "Status", TaskAttribute.TYPE_SINGLE_SELECT, null),
        LATEST_DIFF("latest_diff", "Latest Diff", TaskAttribute.TYPE_CONTAINER, null),
        // DIFF attributes
        SOURCE_FILE("source_file", "Source file", TaskAttribute.TYPE_SHORT_TEXT, null),
        DEST_FILE("dest_file", "Destination file", TaskAttribute.TYPE_SHORT_TEXT, null),
        SOURCE_REVISION("source_revision", "Source revision", TaskAttribute.TYPE_SHORT_TEXT, null),
        DEST_DETAIL("dest_detail", "Destination detail", TaskAttribute.TYPE_SHORT_TEXT, null);

        private final String jsonAttributeName;
        private final String displayName;
        private final String attributeType;
        private final String attributeKind;

        private Attribute(String jsonAttributeName, String displayName, String attributeType, String attributeKind) {

            this.jsonAttributeName = jsonAttributeName;
            this.displayName = displayName;
            this.attributeType = attributeType;
            this.attributeKind = attributeKind;
        }

        public String getJsonAttributeName() {

            return jsonAttributeName;
        }

        public String getDisplayName() {

            return displayName;
        }

        public String getAttributeType() {

            return attributeType;
        }

        public String getAttributeKind() {
         
            return attributeKind;
        }

    }

    private final ReviewboardClientData reviewboardClientData;
    private TimeZone targetTimeZone;
    
    private Map<String, ReviewboardAttributeMapper.Attribute> taskAttributeToMantisAttributes = new HashMap<String, ReviewboardAttributeMapper.Attribute>();
    {
        taskAttributeToMantisAttributes.put(TaskAttribute.PRODUCT, Attribute.REPOSITORY);
        taskAttributeToMantisAttributes.put(TaskAttribute.DESCRIPTION, Attribute.DESCRIPTION);
        taskAttributeToMantisAttributes.put(TaskAttribute.DATE_MODIFICATION, Attribute.LAST_UPDATED);
        taskAttributeToMantisAttributes.put(TaskAttribute.DATE_CREATION, Attribute.TIME_ADDED);
        taskAttributeToMantisAttributes.put(TaskAttribute.SUMMARY, Attribute.SUMMARY);
        taskAttributeToMantisAttributes.put(TaskAttribute.STATUS, Attribute.STATUS);
        taskAttributeToMantisAttributes.put(TaskAttribute.USER_REPORTER, Attribute.SUBMITTER);
    }

    public static DateFormat newDateFormat() {

        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }
    
    public static DateFormat newIso86011DateFormat() {
        
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"); 
    }
    
    public static Date parseDateValue(String value) {
        
        if (value == null)
            return null;

        try {
            return newDateFormat().parse(value);
        } catch (ParseException e) {
            try {
                return new Date(Long.valueOf(value));
            } catch ( NumberFormatException nfe) {
                // ignore, pass-through
            }
            return null;
        }
    }

    public ReviewboardAttributeMapper(TaskRepository taskRepository, ReviewboardClientData reviewboardClientData) {
        this(taskRepository, reviewboardClientData, TimeZone.getDefault());
    }
    
    ReviewboardAttributeMapper(TaskRepository taskRepository, ReviewboardClientData reviewboardClientData, TimeZone targetTimeZone) {
        super(taskRepository);
        this.reviewboardClientData = reviewboardClientData;
        this.targetTimeZone = targetTimeZone;
    }

    @Override
    public String mapToRepositoryKey(TaskAttribute parent, String key) {

        Attribute mapped = taskAttributeToMantisAttributes.get(key);
        if (mapped != null)
            return mapped.toString();

        return super.mapToRepositoryKey(parent, key).toString();
    }

    @Override
    public Date getDateValue(TaskAttribute attribute) {

        return getDateValueFromString(attribute.getValue());
    }

    Date getDateValueFromString(String attributeValue) {
        
        Date parsedDateValue = parseDateValue(attributeValue);
        
        TimeZone siteTimeZone = reviewboardClientData.getTimeZone();
        
        if ( parsedDateValue == null || siteTimeZone == null || targetTimeZone.hasSameRules(siteTimeZone))
            return parsedDateValue;

        long localOffset = this.targetTimeZone.getOffset(parsedDateValue.getTime());
        long siteOffset = siteTimeZone.getOffset(parsedDateValue.getTime());
        
        return new Date(parsedDateValue.getTime() + ( localOffset - siteOffset ));
    }


    @Override
    public IRepositoryPerson getRepositoryPerson(TaskAttribute taskAttribute) {
        
        IRepositoryPerson person = super.getRepositoryPerson(taskAttribute);
        
        setFullName(person);
        
        return person;
    }
    
    private void setFullName(IRepositoryPerson person) {

        // If a new user comments but the user list is not refreshed from the repository the user's
        // full name can not be found ; also handle users with no declared full name
        User user = reviewboardClientData.getUser(person.getPersonId());
        if ( user != null && user.getFullName().length() > 0 )
            person.setName(user.getFullName());
    }

    public IRepositoryPerson getRepositoryPerson(TaskRepository repository, String userName) {
        
        IRepositoryPerson person = repository.createPerson(userName);

        setFullName(person);
        
        return person;
    }
}