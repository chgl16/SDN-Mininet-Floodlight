from mininet.net import Mininet

from mininet.node import OVSSwitch, Controller, RemoteController

from mininet.topolib import TreeTopo

from mininet.log import setLogLevel

from mininet.cli import CLI        

setLogLevel( 'info' )

# Two local and one "external" controller (which is actually c0)

# Ignore the warning message that the remote isn't (yet) running

c0 = RemoteController( 'c0', ip='127.0.0.1')



cmap = { 's1': c0 }

class MultiSwitch( OVSSwitch ):

    "Custom Switch() subclass that connects to different controllers"

    def start( self, controllers ):

        return OVSSwitch.start( self, [ cmap[ self.name ] ] )

topo = TreeTopo( depth=2, fanout=2 )

net = Mininet( topo=topo, switch=MultiSwitch, build=False )

net.build()

net.start()

CLI( net )

net.stop()

