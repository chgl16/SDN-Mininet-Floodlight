package net.floodlightcontroller.mactracker;

import java.util.Collection;
import java.util.Map;

import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFType;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;

// new import
import net.floodlightcontroller.core.IFloodlightProviderService;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.Set;
import net.floodlightcontroller.packet.Ethernet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.floodlightcontroller.topology.TopologyInstance;

public class MACTracker implements IOFMessageListener, IFloodlightModule {

	// ����ӵ�����
	protected IFloodlightProviderService floodlightProvider;  // ��������openflow��Ϣ
	protected Set<Long> macAddresses;    // �����洢��������MAC��ַ
	protected static Logger logger; // �������������Ϣ
	protected static int flag = 1;   // ��¼topo��ӡ����
	
	@Override
	public String getName() {
		// Ϊ���ǵ�OFMessage����������һ��ID
		return MACTracker.class.getSimpleName();
	}

	@Override
	public boolean isCallbackOrderingPrereq(OFType type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCallbackOrderingPostreq(OFType type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		
		return null;
	}

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
		// ��д������ģ���������������
				Collection<Class<? extends IFloodlightService>> l =
				        new ArrayList<Class<? extends IFloodlightService>>();
				    l.add(IFloodlightProviderService.class);
				    
		return l;
	}

	@Override
	public void init(FloodlightModuleContext context) throws FloodlightModuleException {
		// �������ǵ�ģ����Ա������ˡ�һ����˵��һ��ģ�鱻����ʱ��Ҫ���г�ʼ���ġ����Խ������������Գ�ʼ����������ʵ��
		floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
	    macAddresses = new ConcurrentSkipListSet<Long>();
	    logger = LoggerFactory.getLogger(MACTracker.class);
	}

	@Override
	public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
		// ע��һ������PACKET_IN��Ϣ�ļ��������������٣ͣ���
		floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
	}

	@Override
	public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
		/*
		 *  �������ǾͿ��Խ����ǵ�PACKET_IN��Ϣ���ض�����Ϊ���а��ˡ�
		 *  ����receive������ʵ�֣�ע�⣺��������Ҫ����Command.CONTINUE��
		 *  ����������֮��ģ��ſ��Լ���ִ����Ը�PACKET_IN��Ϣ�Ĳ�����
		 */
		Ethernet eth =
                IFloodlightProviderService.bcStore.get(cntx,
                                            IFloodlightProviderService.CONTEXT_PI_PAYLOAD);

        Long sourceMACHash = eth.getSourceMACAddress().getLong();
        if (!macAddresses.contains(sourceMACHash)) {
            macAddresses.add(sourceMACHash);
           // @Lin
            logger.info("MAC Address: {} seen on switch: {}",
                    eth.getSourceMACAddress().toString(),
                    sw.getId().toString());      // ��ע�͵��������� �ã������������������һ���ģ�
            // ����--- @Lin
            String s = "����host: " + eth.getSourceMACAddress().toString() + " <---> " + "������switch: " + sw.getId().toString();
            System.out.println("@Lin " + s);
            
            // ��ӡһ��topo���ɣ����ǽ�����֮�������
            if(flag > 0) {
            	TopologyInstance.printTopology();
            	flag = 0;
            }
            // @Lin
            System.out.println(" ****** һ�ο�ʼ     *****" );
            System.out.println("---HOST---");
            System.out.println("Eth:" + eth.getEtherType().toString());
           // System.out.println("VLanID: " + eth.getVlanID());
           //System.out.println("EtherType: " + eth.getEtherType().toString());
            //System.out.println("Parent: " + eth.getParent().toString());       /////  ���׳��쳣
            System.out.println("Payload: " + eth.getPayload().toString());
           // System.out.println("DestinationMACAddress: " + eth.getDestinationMACAddress().toString());
          //  System.out.println("SourceMACAddress: " + eth.getSourceMACAddress().toString());
           // System.out.println("---SWITCH---");
           // System.out.println("ID: " + sw.getId().toString());
          // System.out.println("Attribute: " + sw.getAttributes().toString());
           //System.out.println("Status: " + sw.getStatus().toString());
            System.out.println(" ****** һ�ν���    *****" );
            
            
            
            ////////////////////////////////
            ////////////////////////////////
//          @Lin ���뵽�ļ�    ���ܶ༸����ǰ�����Ƕ����
            File file1 = new File("./output");
            File file2 = new File("./output/h-sw.txt");
            if(file1.exists()) {
            	
            } else {
            	file1.mkdirs();
            }
            if(file2.exists()) {
            	
            } else {
            	try {
            		file2.createNewFile();
            	} catch(IOException e) {
            		e.printStackTrace();
            	}
            }
            try {
            	FileWriter fileWriter = new FileWriter(file2, true);
            	fileWriter.write(s);
            	fileWriter.write("\r\n");
            	fileWriter.close();
            	
            } catch(IOException e) {
            	e.printStackTrace();
            }
            
            
            
            
        }
        return Command.CONTINUE;
	}

	

}
