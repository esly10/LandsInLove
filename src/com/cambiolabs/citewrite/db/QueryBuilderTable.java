package com.cambiolabs.citewrite.db;

public class QueryBuilderTable {
	
	private String table = null;
	
	private String criteria = null;
	
	private JoinType type = null;
	
	public enum JoinType {

		JOIN("JOIN"), INNER_JOIN("INNER JOIN"), LEFT_JOIN("LEFT JOIN"), RIGHT_JOIN ("RIGHT JOIN");
		
		private String type = "JOIN";
		
		JoinType(String type){
			this.type = type;
		}
		
		public String getType(){
			return this.type;
		}
		
		public String toString(){
			return getType();
		}
		
	};
	
	public QueryBuilderTable(String table, String criteria, JoinType type) {
		super();
		this.table = table;
		this.criteria = criteria;
		this.type = type;
	}
	
	public JoinType getType() {
		return type;
	}
	
	public void setType(JoinType type) {
		this.type = type;
	}
	
	public String getTable() {
		return table;
	}
	
	public void setTable(String table) {
		this.table = table;
	}
	
	public String getCriteria() {
		return criteria;
	}
	
	public void setCriteria(String criteria) {
		this.criteria = criteria;
	}

}
