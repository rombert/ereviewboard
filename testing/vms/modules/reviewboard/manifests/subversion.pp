class reviewboard::subversion {

	exec {'add svn repo':
		command => 'curl -s -u admin:admin --data "name=Local%20SVN&path=http://localhost:5040/svn/repo&tool=Subversion&username=admin&password=admin" http://localhost:5040/api/repositories/',
		path => '/usr/bin',
		unless => 'curl -s http://localhost:5040/api/repositories/ | grep -q http://localhost:5040/svn/repo',
		require => [Class['reviewboard'], Class['subversion::httpd']],
		logoutput => 'on_failure',
	}
}