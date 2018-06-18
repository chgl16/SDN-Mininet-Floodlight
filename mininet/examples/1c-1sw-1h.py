#!/usr/bin/python
# -*- coding: utf-8 -*-

"""
Create a network where different switches are connected to
different controllers, by creating a custom Switch() subclass.
"""

from mininet.net import Mininet
from mininet.node import OVSSwitch, Controller, RemoteController
from mininet.topolib import TreeTopo
from mininet.log import setLogLevel
from mininet.cli import CLI

setLogLevel( 'info' )  # Lin print log

# Two local and one "external" controller (which is actually c0)
# Ignore the warning message that the remote isn't (yet) running
c1 = RemoteController( 'c1', ip='120.79.164.198', port=6653 )
c2 = RemoteController( 'c2', ip='172.20.80.175', port=6653 )
c3 = RemoteController( 'c3', ip='127.0.0.1', port=6653 )

cmap = { 's1': c1, 's2': c2, 's3': c3 }

class MultiSwitch( OVSSwitch ):
    "Custom Switch() subclass that connects to different controllers"
    def start( self, controllers ):
        return OVSSwitch.start( self, [ cmap[ self.name ] ] )

topo = TreeTopo( depth=2, fanout=2 )    # @Lin fanout是分支数
net = Mininet( topo=topo, switch=MultiSwitch, build=False )
for c in [ c1, c2, c3 ]:
    net.addController(c)
net.build()
net.start()
CLI( net )
net.stop()
