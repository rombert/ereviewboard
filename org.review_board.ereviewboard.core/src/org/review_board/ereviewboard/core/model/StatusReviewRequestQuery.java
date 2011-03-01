/*******************************************************************************
 * Copyright (c) 2004 - 2009 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mylyn project committers, Atlassian, Sven Krzyzak
 *******************************************************************************/
/*******************************************************************************
 * Copyright (c) 2009 Markus Knittig
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * Contributors:
 *     Markus Knittig - adapted Trac, Redmine & Atlassian implementations for
 *                      Review Board
 *******************************************************************************/
package org.review_board.ereviewboard.core.model;

import java.util.EnumMap;
import java.util.Map;

/**
 * Abstract class for queries getting review requests by status.
 *
 * @author Markus Knittig
 */
public abstract class StatusReviewRequestQuery implements ReviewRequestQuery {
    
    enum Parameter {
        Status("status"), ToUsers("to-users"),FromUser("from-user"),ToGroups("to-groups"),Repository("repository"),ChangeNum("changenum");
        
        public static Parameter fromString(String value) {
            
            for ( Parameter parameter : Parameter.values() )
                if ( parameter.getParameterName().equals(value))
                    return parameter;
            
            throw new IllegalArgumentException("Unknown query parameter " + value);
        }
        
        private final String parameterName;

        private Parameter(String parameterName) {
            
            this.parameterName = parameterName;
        }
        
        String getParameterName() {
        
            return parameterName;
        }
        
    }
    
    private ReviewRequestStatus status;

    public StatusReviewRequestQuery(ReviewRequestStatus status) {
        super();
        this.status = status;
    }

    public String getQuery() {

        if (status == null)
            return "";

        return "?" + Parameter.Status.getParameterName() + "=" + status.getDisplayname().toLowerCase();
    }

    public void setStatus(ReviewRequestStatus status) {
        this.status = status;
    }
    
    public ReviewRequestStatus getStatus() {
        return status;
    }

    public static ReviewRequestQuery fromQueryString(String queryString) {

        Map<Parameter, String> parameters = parseQueryString(queryString);
        ReviewRequestStatus status = ReviewRequestStatus.parseStatus(parameters.get(Parameter.Status));
        
        if ( parameters.containsKey(Parameter.ToUsers )) {

            String usersString = parameters.get(Parameter.ToUsers);
            
            return new ToUserReviewRequestQuery(status, usersString);
        } else if ( parameters.containsKey(Parameter.FromUser) ) {
            
            String usersString = parameters.get(Parameter.FromUser);
            
            return new FromUserReviewRequestQuery(status, usersString);
        } else if ( parameters.containsKey(Parameter.ToGroups) ) {
            
            String groupsName = parameters.get(Parameter.ToGroups);
            
            return new GroupReviewRequestQuery(status, groupsName);
        } else if ( parameters.containsKey(Parameter.Repository) ) {
            
            int repositoryId = Integer.parseInt(parameters.get(Parameter.Repository));
            int changeNum = Integer.parseInt(parameters.get(Parameter.ChangeNum));
            
            return new RepositoryReviewRequestQuery(status, repositoryId, changeNum);
        }
        
        return new AllReviewRequestQuery(status);
    }
    
    private static Map<Parameter, String> parseQueryString(String queryString) {
        
        Map<Parameter, String> parsedParameters = new EnumMap<StatusReviewRequestQuery.Parameter, String>(Parameter.class);
        
        String[] keyValuePairs = queryString.split("\\&");
        
        for ( String keyValuePair : keyValuePairs ) {
            
            String[] keyValue = keyValuePair.split("=");
            String keyString = keyValue[0];
            if ( keyString.charAt(0) == '?' )
                keyString = keyString.substring(1);
            
            Parameter key = Parameter.fromString(keyString);
            String value = keyValue[1];
            
            parsedParameters.put(key, value);
        }
        
        return parsedParameters;
    }

}
