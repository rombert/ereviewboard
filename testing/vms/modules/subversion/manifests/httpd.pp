class subversion::httpd {

    package {'mod_dav_svn':
        ensure => installed
    }

    file {'/etc/httpd/svn-auth-users':
        source => 'puppet:///modules/subversion/httpd/svn-auth-users',
        notify => Service['httpd']
    }

    file {'/etc/httpd/conf.d/subversion.conf':
        source => 'puppet:///modules/subversion/httpd/subversion.conf',
        notify => Service['httpd']
    }

	file {'/tmp/repo':
		ensure => directory,
		recurse => true,
		source => 'puppet:///modules/subversion/repo-content'
	}

	exec{'import initial repo content':
		command => 'svn import -m "Initial import" --username admin --password admin --non-interactive /tmp/repo http://localhost/svn/repo',
		unless => 'svn ls  --username admin --password admin http://localhost/svn/repo | grep -q simple-project',
		path => '/usr/bin',
		require => [File['/tmp/repo'], Service['httpd']],
		logoutput => 'on_failure'
	}
}