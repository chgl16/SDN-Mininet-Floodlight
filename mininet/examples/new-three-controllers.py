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


def multiControllerNet(con_num=3, sw_num=9, host_num=9):
    "Create a network from semi-scratch with multiple controllers."
    # 创建控制器，交换机，主机的数组
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
    # 改为自定义控制器ip
    c1 = RemoteController('c1', ip='120.79.164.198', port=6653)
    c2 = RemoteController('c2', ip='127.0.0.1', port=6653)    
    c3 = RemoteController('c3', ip='172.20.80.175', port=6653)
    
    # 加入网络
    net.addController(c1)
    net.addController(c2)
    net.addController(c3)

    # 加入控制器数组
    controller_list.append(c1)
    controller_list.append(c2)
    controller_list.append(c3)

    # 创建交换机
    print "*** Creating switches"
    switch_list = [net.addSwitch('s%d' % n) for n in xrange(sw_num)]


    """
    # 多行注释也要缩进，包括注释符
    # 创建主机
    print "*** Creating hosts"
    host_list = [net.addHost('h%d' % n) for n in xrange(host_num)]

    # 创建交换机和主机的连接
    print "*** Creating links of host2switch." #改为一对一
    for i in xrange(0, sw_num):
        net.addLink(switch_list[i], host_list[i])  
        #net.addLink(switch_list[i], host_list[i*2+1])
    """

    # 创建交换机和交换机的连接
    print "*** Creating interior links of switch2switch."
    for i in xrange(0, sw_num, sw_num/con_num):
        for j in xrange(sw_num/con_num):
            for k in xrange(sw_num/con_num):
                if j != k and j > k:
                    net.addLink(switch_list[i+j], switch_list[i+k])

    # 创建来自三个控制器管理的交换机连接
    print "*** Creating intra links of switch2switch."

    # 改
    # 用首末两个交换机和外部网络链接，一个网络链接一条
    # c1:0,2  c2:3,5  c3:6,8 
    # c1 -> others
    net.addLink(switch_list[0], switch_list[3])
    net.addLink(switch_list[3], switch_list[6])
    


    # 启动网络
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
    multiControllerNet(con_num=3, sw_num=9, host_num=9)
