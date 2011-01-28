package org.elegosproject.romupdater.types;

public class RepoList {
	private String model;
	repoElement[] repositories;
	
	public static class repoElement {
		String name;
		String url;
		
		public String getName() {
			return name;
		}
		
		public String getUrl() {
			return url;
		}
	}
	
	public String getModel() {
		return model;
	}
	
	public repoElement[] getRepositories() {
		return repositories;
	}
}
