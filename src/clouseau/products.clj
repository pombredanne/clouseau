(ns clouseau.products)

(def products [
     ["Fedora 20"
        {:classname   "org.sqlite.JDBC"
         :subprotocol "sqlite"
         :subname     "packages/f20/primary.sqlite"
     }]
     ["RHEL 6"
        {:classname   "org.sqlite.JDBC"
         :subprotocol "sqlite"
         :subname     "packages/rhel6/primary.sqlite"
     }]
     ["RHEL 6 Optional"
        {:classname   "org.sqlite.JDBC"
         :subprotocol "sqlite"
         :subname     "packages/rhel6_opt/primary.sqlite"
     }]
     ["RHEL 7"
        {:classname   "org.sqlite.JDBC"
         :subprotocol "sqlite"
         :subname     "packages/rhel7/primary.sqlite"
     }]
     ["RHEL 7 Optional"
        {:classname   "org.sqlite.JDBC"
         :subprotocol "sqlite"
         :subname     "packages/rhel7_opt/primary.sqlite"
     }]
     ["RHEL 7.1"
        {:classname   "org.sqlite.JDBC"
         :subprotocol "sqlite"
         :subname     "packages/rhel71/primary.sqlite"
     }]
     ["RHEL 7.1 Optional"
        {:classname   "org.sqlite.JDBC"
         :subprotocol "sqlite"
         :subname     "packages/rhel71_opt/primary.sqlite"
     }]
     ["RHEL 7.1 Realtime"
        {:classname   "org.sqlite.JDBC"
         :subprotocol "sqlite"
         :subname     "packages/rhel7_rt/primary.sqlite"
     }]
     ["RHSCL 2 for RHEL 6"
        {:classname   "org.sqlite.JDBC"
         :subprotocol "sqlite"
         :subname     "packages/rhel6_rhscl2/primary.sqlite"
     }]
     ["RHSCL 2 for RHEL 7"
        {:classname   "org.sqlite.JDBC"
         :subprotocol "sqlite"
         :subname     "packages/rhel7_rhscl2/primary.sqlite"
     }]
     ["Satellite 6 for RHEL 7"
        {:classname   "org.sqlite.JDBC"
         :subprotocol "sqlite"
         :subname     "packages/rhel7_satellite6/primary.sqlite"
     }]
])

