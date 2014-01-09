class subversion {

    package {'subversion':
        ensure => installed
    }

    file {'/var/www/svn':
        ensure => directory,
        owner => apache,
        group => apache,
        require => Package['httpd']
    }

    exec {'svnadmin create':
        command => 'svnadmin create /var/www/svn/repo',
        creates => '/var/www/svn/repo',
        path    => '/usr/bin',
        user   => 'apache',
        logoutput => 'on_failure',
        require => [File['/var/www/svn'], Package['subversion']]
    }
}
