package org.review_board.ereviewboard.core.client;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Robert Munteanu
 */
class QueryBuilder {
    
    public static QueryBuilder fromString(String rawQuery) {

        String base;
        String parameters;
        int parametersStart = rawQuery.indexOf('?');
        if ( parametersStart != - 1) {
            base = rawQuery.substring(0, parametersStart);
            parameters = rawQuery.substring(parametersStart + 1);
        } else {
            base = rawQuery;
            parameters = "";
        }
        
        QueryBuilder builder = new QueryBuilder(base);
        if ( parameters.length() != 0 ) {
            String[] pairs = parameters.split("&");
            for ( String pair : pairs ) {
                String[] keyValue = pair.split("=");
                builder.setParameter(keyValue[0], keyValue[1]);
            }
        }
        
        return builder;
    }
	
	private final StringBuilder _query = new StringBuilder();
	private final Map<String, String> _parameters = new HashMap<String, String>();
	
	public QueryBuilder() {
	    
	}
	
	public QueryBuilder(String query) {
		
		_query.append(query);
	}
	
	public QueryBuilder append(String query) {
		
		_query.append(query);
		
		return this;
	}
	
	public QueryBuilder append(char query) {
	    
	    _query.append(query);
	    
	    return this;
	}
	
	public QueryBuilder append(int query) {
	    
	    _query.append(query);
	    
	    return this;
	}
	
	public QueryBuilder setParameter(String name, String value) {
		
		_parameters.put(name, value);
		
		return this;
	}
	
	public QueryBuilder setParameter(String name, int value) {
	    
	    _parameters.put(name, String.valueOf(value));
	    
	    return this;
	}
	
	public String createQuery() {

		if ( _query.length() == 0 && _parameters.size() == 0 )
			throw new IllegalArgumentException("Empty or already created query.");
		
		if ( _parameters.size() > 0 )
		    _query.append('?');
		
		for ( Map.Entry<String, String> parameterEntry : _parameters.entrySet() )
            _query.append(parameterEntry.getKey()).append("=").append(parameterEntry.getValue()).append('&');
		
		if ( _parameters.size() > 0 )
		    _query.deleteCharAt(_query.length() - 1);
		
		_parameters.clear();
		
		String result = _query.toString();
		
		_query.setLength(0);
		
		return result;
	}

    public QueryBuilder clearParameters() {
        
        _parameters.clear();
        
        return this;
    }

    public void ensureTrailingSlash() {

        if ( ( _query.length() > 0 && _query.charAt(_query.length() - 1) == '/') || _query.indexOf("?") != -1)
            return;
        
        _query.append('/');
    }
}