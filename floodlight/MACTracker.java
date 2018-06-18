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

	// 自添加的属性
	protected IFloodlightProviderService floodlightProvider;  // 用来监听openflow信息
	protected Set<Long> macAddresses;    // 用来存储监听到的MAC地址
	protected static Logger logger; // 用来输出监听信息
	protected static int flag = 1;   // 记录topo打印次数
	
	@Override
	public String getName() {
		// 为我们的OFMessage监听器放入一个ID
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
		// 重写，告诉模块加载器，依赖他
				Collection<Class<? extends IFloodlightService>> l =
				        new ArrayList<Class<? extends IFloodlightService>>();
				    l.add(IFloodlightProviderService.class);
				    
		return l;
	}

	@Override
	public void init(FloodlightModuleContext context) throws FloodlightModuleException {
		// 现在我们的模块可以被加载了。一般来说，一个模块被加载时是要进行初始化的。所以接下来我们来对初始化方法进行实现
		floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
	    macAddresses = new ConcurrentSkipListSet<Long>();
	    logger = LoggerFactory.getLogger(MACTracker.class);
	}

	@Override
	public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
		// 注册一个监听PACKET_IN消息的监听器，用来跟踪ＭＡＣ
		floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
	}

	@Override
	public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
		/*
		 *  现在我们就可以叫我们的PACKET_IN消息与特定的行为进行绑定了。
		 *  这在receive方法中实现，注意：我们这里要返回Command.CONTINUE，
		 *  这样在我们之后模块才可以继续执行针对该PACKET_IN消息的操作：
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
                    sw.getId().toString());      // 先注释掉，这类似 Ｃ＃的输出和下面的输出是一样的，
            // 测试--- @Lin
            String s = "主机host: " + eth.getSourceMACAddress().toString() + " <---> " + "交换机switch: " + sw.getId().toString();
            System.out.println("@Lin " + s);
            
            // 打印一次topo即可，就是交换机之类的问题
            if(flag > 0) {
            	TopologyInstance.printTopology();
            	flag = 0;
            }
            // @Lin
            System.out.println(" ****** 一次开始     *****" );
            System.out.println("---HOST---");
            System.out.println("Eth:" + eth.getEtherType().toString());
           // System.out.println("VLanID: " + eth.getVlanID());
           //System.out.println("EtherType: " + eth.getEtherType().toString());
            //System.out.println("Parent: " + eth.getParent().toString());       /////  会抛出异常
            System.out.println("Payload: " + eth.getPayload().toString());
           // System.out.println("DestinationMACAddress: " + eth.getDestinationMACAddress().toString());
          //  System.out.println("SourceMACAddress: " + eth.getSourceMACAddress().toString());
           // System.out.println("---SWITCH---");
           // System.out.println("ID: " + sw.getId().toString());
          // System.out.println("Attribute: " + sw.getAttributes().toString());
           //System.out.println("Status: " + sw.getStatus().toString());
            System.out.println(" ****** 一次结束    *****" );
            
            
            
            ////////////////////////////////
            ////////////////////////////////
//          @Lin 输入到文件    可能多几个，前几个是多余的
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
