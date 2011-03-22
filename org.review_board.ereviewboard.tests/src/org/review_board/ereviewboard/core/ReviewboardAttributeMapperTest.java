package org.review_board.ereviewboard.core;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;

import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.junit.Test;
import org.review_board.ereviewboard.core.client.ReviewboardClientData;

public class ReviewboardAttributeMapperTest {

    private ReviewboardAttributeMapper mapper;

    public void prepareMapper(TimeZone clientTimeZone) {

        TaskRepository repository = new TaskRepository(ReviewboardCorePlugin.REPOSITORY_KIND,
                "http://localhost/");
        ReviewboardClientData clientData = new ReviewboardClientData();
        clientData.setTimeZone(clientTimeZone);

        mapper = new ReviewboardAttributeMapper(repository, clientData,
                TimeZone.getTimeZone("Europe/London"));

    }

    @Test
    public void getDateValueForNull() {

        prepareMapper(TimeZone.getTimeZone("America/Los_Angeles"));

        assertThat(mapper.getDateValueFromString(null), is(nullValue()));
    }

    @Test
    public void getDateValueForDifferentTimeZone() throws ParseException {

        prepareMapper(TimeZone.getTimeZone("America/Los_Angeles"));

        String dateString = "2010-08-28 02:26:18";
        String expectedDateString = "2010-08-28 10:26:18";

        Date expectedDate = ReviewboardAttributeMapper.newDateFormat().parse(expectedDateString);

        assertThat(mapper.getDateValueFromString(dateString), is(expectedDate));
    }

    @Test
    public void getDateValueForNullClientTimeZone() throws ParseException {

        prepareMapper(null);

        String dateString = "2010-08-28 02:26:18";
        String expectedDateString = dateString;

        Date expectedDate = ReviewboardAttributeMapper.newDateFormat().parse(expectedDateString);

        assertThat(mapper.getDateValueFromString(dateString), is(expectedDate));
    }
    
    @Test
    public void getDateValueForSameClientTimeZone() throws ParseException {

        prepareMapper(TimeZone.getTimeZone("Europe/London"));

        String dateString = "2010-08-28 02:26:18";
        String expectedDateString = dateString;

        Date expectedDate = ReviewboardAttributeMapper.newDateFormat().parse(expectedDateString);

        assertThat(mapper.getDateValueFromString(dateString), is(expectedDate));
    }
}
