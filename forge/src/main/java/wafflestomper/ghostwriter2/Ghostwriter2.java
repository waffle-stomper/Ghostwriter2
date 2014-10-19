package wafflestomper.ghostwriter2;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreenBook;
import net.minecraftforge.common.MinecraftForge;
import wafflestomper.ghostwriter2.gbook.GBook;
import wafflestomper.ghostwriter2.gbook.GLine;
import wafflestomper.ghostwriter2.gbook.GPage;
import wafflestomper.ghostwriter2.gui.GuiGhostBook;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.PlayerTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.RenderTickEvent;


@Mod(modid = Ghostwriter2.MODID, version = Ghostwriter2.VERSION, name = Ghostwriter2.NAME)
public class Ghostwriter2{
	
    public static final String MODID = "Ghostwriter2";
    public static final String VERSION = "1.7.10-0.9.1";
    public static final String NAME = "Ghostwriter2";
    
    private Minecraft mc = Minecraft.getMinecraft();
	private int connectWait = 10;
	private boolean connected = false;
	private int firstGuiOpenWait = 20;
	private boolean firstGuiOpen = false;
	private GBook bookClipboard;
	private List<String> pageClipboard;
	
	
	public Ghostwriter2(){
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);
		this.bookClipboard = new GBook();
		this.pageClipboard = new ArrayList();
	}
	

	@SubscribeEvent
    public void tick(PlayerTickEvent event) 
    {
    	if (event.phase == Phase.START){
    		
    		//######################################################
    		//######################################################
    		//######################################################
    		//Replace the default book GUI with the Ghostwriter GUI
    		//               DO NOT REMOVE THIS!!!!
    		//######################################################
    		//######################################################
    		//######################################################
            if (this.mc.currentScreen instanceof GuiScreenBook) {
            	//Disabled for testing
                mc.displayGuiScreen(new GuiGhostBook(this.bookClipboard, this.pageClipboard));
            }
            //######################################################
            //######################################################
            //######################################################
            //######################################################
            //######################################################
            
            
            //This is used in debugging to open the Ghostwriter GUI automatically when the client connects to a server
            //It should be disabled when building a release candidate
    		if (!firstGuiOpen){
    			if (firstGuiOpenWait-- <= 0){
    				firstGuiOpen = true;
    				//this.mc.displayGuiScreen(new GuiGhostBook(this.bookClipboard, this.pageClipboard));
    			}
    		}	
    	}
    }
    
    @SubscribeEvent
    public void renderTick(RenderTickEvent event){
    	if (event.phase == Phase.END){
    		
    		//This is used when debugging to automatically connect to the local test server. It should also be disabled when building a release candidate.
    		if (!this.connected){
    			if (connectWait-- <= 0){
    				//FMLClientHandler.instance().connectToServerAtStartup("localhost", 25565);
    	    		this.connected = true;
    	    	}
    		}
    	}
    }
	
	
	
    
}
