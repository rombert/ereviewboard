package org.review_board.ereviewboard.subclipse.internal.wizards;

import org.eclipse.core.resources.IProject;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;

public class BranchInformationFinderFactory {
	public static BranchInformationFinder finderFor(IProject p) {
		ISVNLocalResource projectSvnResource = SVNWorkspaceRoot.getSVNResourceFor(p);

		String projectRoot = projectSvnResource.getRepository().getRepositoryRoot().toString();
		
		if (projectRoot != null && projectRoot.toLowerCase().startsWith("http://")) {
			// For URL SVN based repo this is a way to calculate
			return new HTTPBranchInformationFinder(projectRoot, projectSvnResource);
		}
		
		// for other SVN repository kinds, I actually don't know how to do
		return new NoInfoBranchInformationFinder();
	}
	
	private static class NoInfoBranchInformationFinder implements BranchInformationFinder {
		public String getBranchName() {
			return "";
		}
	}
	
	private static class HTTPBranchInformationFinder implements BranchInformationFinder {
		private String branchName;

		HTTPBranchInformationFinder(String projectRoot, ISVNLocalResource projectSvnResource) {
			String fullURL = projectSvnResource.getUrl().toString();
			branchName = fullURL.replace(projectRoot, "");
		}

		public String getBranchName() {
			return branchName;
		}
	}
}
