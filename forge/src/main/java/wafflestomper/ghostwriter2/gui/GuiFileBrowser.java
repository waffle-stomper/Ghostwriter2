package wafflestomper.ghostwriter2.gui;

import java.io.File;
import java.util.List;

import org.lwjgl.input.Keyboard;

import wafflestomper.ghostwriter2.FileHandler;
import wafflestomper.ghostwriter2.gbook.GBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.Tessellator;

public class GuiFileBrowser extends GuiScreen{
	
	private List<File> listItems;
	private GuiFileBrowser.ScrollList scrollList;
	public int slotSelected = -1;
	
	private static final int BUTTONWIDTH = 60;
	private static final int BUTTONHEIGHT = 20;
	private static final int BTN_LOAD = 0;
	private static final int BTN_CANCEL = 1;
	
	private GuiButton btnLoad;
	private GuiButton btnCancel;
	
	private FileHandler fileHandler = new FileHandler();
	private GuiGhostBook parentScreen;
	private GBook tempBook;
	
	private String displayPath = "";
	private String previewTitle = "";
	private String previewAuthor = "";
	private String previewPage = "";
	
	
	public GuiFileBrowser(GuiGhostBook _parentScreen){
		this.parentScreen = _parentScreen;
	}
	
	
	public void initGui(){
        this.fileHandler.currentPath = this.fileHandler.getDefaultPath();
        this.displayPath = this.fileHandler.currentPath.getAbsolutePath();
        this.buttonList.add(btnLoad = new GuiButton(BTN_LOAD, this.width-(BUTTONWIDTH+5), this.height-50, BUTTONWIDTH, BUTTONHEIGHT, "Load"));
        this.buttonList.add(btnCancel = new GuiButton(BTN_CANCEL, this.width-(BUTTONWIDTH+5), this.height-25, BUTTONWIDTH, BUTTONHEIGHT, "Cancel"));
        
        //Add buttons for each non-empty drive letter
        int rootNum = 100;
        List<File> roots = this.fileHandler.getValidRoots();
        for (File root : roots){
        	this.buttonList.add(new GuiButton(rootNum, 5, 35 + 21*(rootNum-100), 50, 20, root.getAbsolutePath()));
        	rootNum++;
        }
        populateFileList();
        this.scrollList = new ScrollList();
        this.scrollList.registerScrollButtons(4, 5);
	}
	
	/** Gets the directory listing for the current directory and puts it in this.listItems ready for display */
	private void populateFileList(){
		this.listItems = this.fileHandler.listFiles(this.fileHandler.currentPath);		
	}
	
	/**
	 * Tries to load the file at <file>. If successful, the title, author, and a preview of the first page will be displayed. 
	 * The book loaded for the preview is passed to the parent screen if the user clicks 'load' or double-clicks the filename.
	 * */
	private void loadPreview(File file){
		this.tempBook = this.fileHandler.loadBook(file);
		if(this.tempBook != null){
			this.previewAuthor = this.tempBook.author;
			this.previewTitle = this.tempBook.title;
			String firstPage = GBook.removeFormatting(this.tempBook.pages.get(0).asString().replaceAll("\n", " "));
			this.previewPage = GBook.truncateStringPixels(firstPage, "...", 200, false);
		}
		else{
			this.previewTitle = "";
    		this.previewAuthor = "";
    		this.previewPage = "";
		}
	}
	
	
	private void goBackToParentGui(){
		this.mc.displayGuiScreen(this.parentScreen);
	}
	
	
    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     */
    protected void keyTyped(char par1, int par2){
        if (par2 == Keyboard.KEY_ESCAPE){
        	goBackToParentGui();
        }
    }
    
    
	public void drawScreen(int par1, int par2, float par3){
		this.displayPath = this.fileHandler.currentPath.getAbsolutePath();
		this.btnLoad.enabled = this.tempBook != null;
		populateFileList();
		this.scrollList.drawScreen(par1, par2, par3);
		super.drawScreen(par1, par2, par3);
		//Draw the current path to the top of the screen
		this.drawCenteredString(this.fontRendererObj, GBook.truncateStringPixels(this.displayPath,"...", 200, true), this.width / 2, 20, 0xDDDDDD);
		if (!this.previewAuthor.equals("") || !this.previewTitle.equals("") || !this.previewPage.equals("")){
			this.drawCenteredString(this.fontRendererObj, "Author: " + this.previewAuthor, this.width / 2, this.height-50, 0xFFFFFF);
			this.drawCenteredString(this.fontRendererObj, "Title: " + this.previewTitle, this.width / 2, this.height-40, 0xFFFFFF);
			this.drawCenteredString(this.fontRendererObj, "Page 1: " + this.previewPage, this.width / 2, this.height-30, 0xFFFFFF);
		}
    }
	
	
	/** Called when a button is clicked */
    protected void actionPerformed(GuiButton buttonPressed){
    	if (!buttonPressed.enabled){return;}
    	
    	switch (buttonPressed.id){
    		case BTN_LOAD:
    			if (this.tempBook != null){
        			this.parentScreen.setBook(GuiFileBrowser.this.tempBook);
        			Minecraft.getMinecraft().displayGuiScreen(this.parentScreen);
        		}
    			break;
    		case BTN_CANCEL:
    			Minecraft.getMinecraft().displayGuiScreen(this.parentScreen);
    			break;
    		default:
    			break;
    	}
    	
    	//Handle the drive letter buttons
    	if (buttonPressed.id >= 100){
    		this.fileHandler.currentPath = new File(buttonPressed.displayString);
    	}
    }
	
	
    class ScrollList extends GuiSlot{
    	
    	private static final int SLOT_HEIGHT = 12;
    	
    	
        public ScrollList(){
        	super(GuiFileBrowser.this.mc, GuiFileBrowser.this.width, GuiFileBrowser.this.height, 32, GuiFileBrowser.this.height - 64, SLOT_HEIGHT);
        }

        
        /** 
         * Returns the number of slots in the list, with a minimum so that the position of the first slot is always consistent, even
         * if the current directory is empty
         */
        protected int getPaddedSize(){
        	int scrollHeight = GuiFileBrowser.this.height - 96;
        	int minSlots = (int)Math.ceil(scrollHeight/SLOT_HEIGHT);
        	
        	if (GuiFileBrowser.this.listItems.size() >= minSlots){
        		return GuiFileBrowser.this.listItems.size();
        	}
        	else{
        		return minSlots;
        	}
        }
        
        
        /** Returns the number of slots in the list */
        protected int getSize(){
            return getPaddedSize();
        }

        
        /** Called when a slot is (double)clicked */
        protected void elementClicked(int slotClicked, boolean doubleClicked, int clickXPos, int clickYPos){
        	this.setShowSelectionBox(true);
            if (doubleClicked){
            	if (slotClicked == 0){
            		//Go up to the parent directory
                	GuiFileBrowser.this.fileHandler.navigateUp();
                	GuiFileBrowser.this.slotSelected = -1;
                	this.setShowSelectionBox(false);
                	return;
                }
                else if (slotClicked <= GuiFileBrowser.this.listItems.size()){
                	File itemClicked = GuiFileBrowser.this.listItems.get(slotClicked-1);
                	if (itemClicked.isDirectory()){
                		//Go into the clicked directory
                		GuiFileBrowser.this.fileHandler.currentPath = itemClicked;
                		GuiFileBrowser.this.slotSelected = -1;
                		this.setShowSelectionBox(false);
                		return;
                	}
                	else{
                		//We've double-clicked on a file
                		//It should already be loaded in if it's a real file, so it just needs to be passed to GuiGhostBook
                		if (GuiFileBrowser.this.tempBook != null){
                			GuiFileBrowser.this.parentScreen.setBook(GuiFileBrowser.this.tempBook);
                			Minecraft.getMinecraft().displayGuiScreen(GuiFileBrowser.this.parentScreen);
                		}
                	}
                }
            }
            else if (slotClicked > 0 && slotClicked <= GuiFileBrowser.this.listItems.size()){
            	//A file or directory has been single-clicked
            	File selectedFile = GuiFileBrowser.this.listItems.get(slotClicked-1);
            	if (selectedFile.isFile() && !isSelected(slotClicked)){
            		GuiFileBrowser.this.loadPreview(selectedFile);
            	}
            	else{
            		GuiFileBrowser.this.previewTitle = "";
            		GuiFileBrowser.this.previewAuthor = "";
            		GuiFileBrowser.this.previewPage = "";
            		GuiFileBrowser.this.tempBook = null;
            	}
            }
            else{
            	//I am a hacker, in the worst sense of the word.
            	//TODO: Fix this repetition
        		GuiFileBrowser.this.previewTitle = "";
        		GuiFileBrowser.this.previewAuthor = "";
        		GuiFileBrowser.this.previewPage = "";
        		GuiFileBrowser.this.tempBook = null;
        	}
            GuiFileBrowser.this.slotSelected = slotClicked;
        }

        
        /**
         * Returns true if the element passed in is currently selected
         */
        protected boolean isSelected(int pos){
            return pos == GuiFileBrowser.this.slotSelected;
        }

        
        /**
         * Return the height of the content being scrolled
         * This is used to determine the scrollable area. It doesn't affect the actual slot height
         */
        protected int getContentHeight(){
            return getPaddedSize() * SLOT_HEIGHT;
        }

        
        protected void drawBackground(){
            GuiFileBrowser.this.drawDefaultBackground();
        }

        
        protected void drawSlot(int slotNum, int p_148126_2_, int p_148126_3_, int p_148126_4_, Tessellator p_148126_5_, int p_148126_6_, int p_148126_7_){
        	List<File> list = GuiFileBrowser.this.listItems;
        	//Empty padding slots at the bottom
        	if (slotNum > list.size()){return;}
        	
        	String slotText = "";
        	int color = 0xFFFFFF;
        	
        	if (slotNum == 0){
        		slotText = "..";
        		color = 0x00FF00;
        	}
        	else{
        		slotText = GBook.truncateStringPixels(list.get(slotNum-1).getName(), "...", 200, false);
        		if (list.get(slotNum-1).isFile()){
    				color = 0xFF0000;
    			}
    			else{
    				color = 0x00FF00;
    			}
        	}
        	
            GuiFileBrowser.this.drawString(GuiFileBrowser.this.fontRendererObj, slotText, p_148126_2_ + 2, p_148126_3_ + 1, color);
        }
    }

}
