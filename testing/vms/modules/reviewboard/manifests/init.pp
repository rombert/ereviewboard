class reviewboard {

    # Install ReviewBoard package
    package {'ReviewBoard':
        ensure => installed
    }

    # Install and enable Apache

    package {'httpd':
        ensure => installed
    }

    service {'httpd':
        ensure => running,
        enable => true,
        require => Package['httpd']
    }

    package {'mod_wsgi':
        ensure => installed
    }

    exec {'enable http in firewall':
        command => 'firewall-cmd --add-service=http',
        unless => 'firewall-cmd --query-service=http',
        user => 'root',
        path => ['/bin','/usr/bin'],
        require => Package['httpd']
    }


    # Install and configure reviewboard
    exec {'rb-site':
        command => 'rb-site install --noinput --domain-name=localhost --db-type=mysql --db-host=localhost --db-name=reviewboard --db-user=reviewboard --db-pass=rb --cache-type=file --cache-info=/var/cache/reviewboard/cache --web-server-type=apache --python-loader=wsgi --admin-user=admin --admin-password=admin --admin-email=robert@lmn.ro /var/www/reviews/',
        path => '/usr/bin/',
        user => 'root',
        creates => '/var/www/reviews',
        require => [ Package['ReviewBoard'], Mysql_user['reviewboard@localhost'], Mysql_grant['reviewboard/reviewboard.*'] ],
        notify => Service['httpd'],
        logoutput => 'on_failure'
    }

    exec {'copy apache-wsgi.conf':
        command => 'cp /var/www/reviews/conf/apache-wsgi.conf /etc/httpd/conf.d/apache-wsgi.conf',
        path => ['/bin/','/usr/bin/'],
        creates => '/etc/httpd/conf.d/apache-wsgi.conf',
        require => Exec['rb-site']
    }

    file {'/var/cache/reviewboard/cache':
        ensure => directory,
        owner => apache,
        require => Exec['rb-site']
    }
    
    file {'/var/www/reviews/htdocs/media/uploaded':
        ensure => directory,
        owner => apache,
        recurse => true,
        require => Exec['rb-site']
    }

    file {'/var/www/reviews/htdocs/media/ext':
        ensure => directory,
        owner => apache,
        require => Exec['rb-site']
    }

    file {'/var/www/reviews/data':
        ensure => directory,
        owner => apache,
        require => Exec['rb-site']
    }

    Class['reviewboard'] -> Class['mysql::bindings']
}
