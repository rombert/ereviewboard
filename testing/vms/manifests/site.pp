
class { 'mysql::server':
    root_password => 'reviewboard',
    service_enabled => true,
    users => {
        'reviewboard@localhost' => {
            ensure => 'present',
            password_hash => '*EBA7736036A781E61E6F53E1674F48A5D3512342' # rb
        }
    },
    databases => {
        'reviewboard' => {
            ensure => 'present'
        }
    },
    grants => {
        'reviewboard/reviewboard.*' => {
            ensure => 'present',
            privileges => ['ALL'],
            table => 'reviewboard.*',
            user  => 'reviewboard@localhost'
        }
    }
}

class { 'mysql::bindings':
    python_enable => true
}

class { 'selinux': 
    mode => 'disabled'
}

include reviewboard
include reviewboard::subversion
include subversion
include subversion::httpd