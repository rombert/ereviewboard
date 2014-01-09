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
	
	Class['subversion::httpd'] -> Class['subversion']
	Class['subversion::httpd'] -> Package['httpd']
}