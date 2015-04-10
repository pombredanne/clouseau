;;;
;;;   Clouseau
;;; 
;;;    Copyright (C) 2015 Pavel Tisnovsky <ptisnovs@redhat.com>
;;; 
;;; Bytecode synth is free software; you can redistribute it and/or modify
;;; it under the terms of the GNU General Public License as published by
;;; the Free Software Foundation; either version 2, or (at your option)
;;; any later version.
;;; 
;;; Bytecode synth is distributed in the hope that it will be useful, but
;;; WITHOUT ANY WARRANTY; without even the implied warranty of
;;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
;;; General Public License for more details.
;;; 
;;; You should have received a copy of the GNU General Public License
;;; along with Bytecode synth; see the file COPYING.  If not, write to the
;;; Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
;;; 02110-1301 USA.
;;; 
;;; Linking this library statically or dynamically with other modules is
;;; making a combined work based on this library.  Thus, the terms and
;;; conditions of the GNU General Public License cover the whole
;;; combination.
;;; 
;;; As a special exception, the copyright holders of this library give you
;;; permission to link this library with independent modules to produce an
;;; executable, regardless of the license terms of these independent
;;; modules, and to copy and distribute the resulting executable under
;;; terms of your choice, provided that you also meet, for each linked
;;; independent module, the terms and conditions of the license of that
;;; module.  An independent module is a module which is not derived from
;;; or based on this library.  If you modify this library, you may extend
;;; this exception to your version of the library, but you are not
;;; obligated to do so. If you do not wish to do so, delete this
;;; exception statement from your version.
;;; 

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
     ["RHEL 6.7"
        {:classname   "org.sqlite.JDBC"
         :subprotocol "sqlite"
         :subname     "packages/rhel67/primary.sqlite"
     }]
     ["RHEL 6 High Availability"
        {:classname   "org.sqlite.JDBC"
         :subprotocol "sqlite"
         :subname     "packages/rhel67_ha/primary.sqlite"
     }]
     ["RHEL 6 Load Balancer"
        {:classname   "org.sqlite.JDBC"
         :subprotocol "sqlite"
         :subname     "packages/rhel67_lb/primary.sqlite"
     }]
     ["RHEL 6 Resilient Storage"
        {:classname   "org.sqlite.JDBC"
         :subprotocol "sqlite"
         :subname     "packages/rhel67_rs/primary.sqlite"
     }]
     ["RHEL 6 Scalable File System"
        {:classname   "org.sqlite.JDBC"
         :subprotocol "sqlite"
         :subname     "packages/rhel67_cfs/primary.sqlite"
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

