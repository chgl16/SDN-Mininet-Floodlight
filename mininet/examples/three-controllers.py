#!/usr/bin/python
# -*- coding: utf-8 -*-
"""
   This example create 3 sub-networks to connect 3  domain controllers.
   Each domain network contains at least 5 switches.
   For an easy test, we add 2 hosts for one switch.
   So, in our topology, we have at least 15 switches and 30 hosts.    
"""

from mininet.net import Mininet
from mininet.node import Controller, RemoteController, OVSSwitch
from mininet.cli import CLI
from mininet.log import setLogLevel, info
from mininet.link import Link, Intf, TCLink
from mininet.topo import Topo
import logging
import os


def multiControllerNet(con_num=3, sw_num=15, host_num=30):
    "Create a network from semi-scratch with multiple controllers."
    controller_list = []
    switch_list = []
    host_list = []

    net = Mininet(controller=None, switch=OVSSwitch, link=TCLink)

    # 改
    '''   这是原来自动控制器的代码
    for i in xrange(con_num):
        name = 'controller%s' % str(i)
        c = net.addController(name, controller=RemoteController,port=6661 + i)
        controller_list.append(c)
        print "*** Creating %s" % name
    '''
    # 改为自定义ip的
    c1 = RemoteController('c1', ip='120.79.164.198', port=6653)
    c2 = RemoteController('c2', ip='127.0.0.1', port=6653)    
    c3 = RemoteController('c3', ip='172.20.80.175', port=6653)
    
    # 加入网络
    net.addController(c1)
    net.addController(c2)
    net.addController(c3)
    controller_list.append(c1)
    controller_list.append(c2)
    controller_list.append(c3)
    print "*** Creating switches"
    switch_list = [net.addSwitch('s%d' % n) for n in xrange(sw_num)]

    print "*** Creating hosts"
    host_list = [net.addHost('h%d' % n) for n in xrange(host_num)]

    print "*** Creating links of host2switch."
    for i in xrange(0, sw_num):
        net.addLink(switch_list[i], host_list[i*2])
        net.addLink(switch_list[i], host_list[i*2+1])

    print "*** Creating interior links of switch2switch."
    for i in xrange(0, sw_num, sw_num/con_num):
        for j in xrange(sw_num/con_num):
            for k in xrange(sw_num/con_num):
                if j != k and j > k:
                    net.addLink(switch_list[i+j], switch_list[i+k])

    print "*** Creating intra links of switch2switch."

    # 改
    # 用首末两个交换机和外部网络链接，一个网络链接一条
    # c1:0,4  c2:5,9  c3:10,14 
    # c1 -> others
    net.addLink(switch_list[0], switch_list[5])
    net.addLink(switch_list[4], switch_list[10])
    

    # c2 -> others
 

    # c3 has not need to add links.

    print "*** Starting network"
    net.build()
    for c in controller_list:
        c.start()

    _No = 0
    for i in xrange(0, sw_num, sw_num/con_num):
        for j in xrange(sw_num/con_num):
            switch_list[i+j].start([controller_list[_No]])
        _No += 1

    #print "*** Testing network"
    #net.pingAll()

    print "*** Running CLI"
    CLI(net)

    print "*** Stopping network"
    net.stop()

if __name__ == '__main__':
    setLogLevel('info')  # for CLI output
    multiControllerNet(con_num=3, sw_num=15, host_num=30)
