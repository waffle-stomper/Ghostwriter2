package wafflestomper.ghostwriter2.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import scala.reflect.internal.Trees.This;
import wafflestomper.ghostwriter2.FileHandler;
import wafflestomper.ghostwriter2.Printer;
import wafflestomper.ghostwriter2.gbook.GBook;
import wafflestomper.ghostwriter2.gbook.GLine;
import wafflestomper.ghostwriter2.gbook.GPage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenBook;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.common.MinecraftForge;

public class GuiGhostBook extends GuiScreen{
	
    private Minecraft mc = Minecraft.getMinecraft();
    
    private int updateCount = 0;
    
    private int bookImageWidth = 192;
    private int bookImageHeight = 192;
    private static final ResourceLocation bookGuiTextures = new ResourceLocation("textures/gui/book.png");
    private static final String PAGE_INDICATOR_TEMPLATE = "Page %d of %d";
    
    //Used for copying multiple pages at a time
    private int selectedPageA = -1;
    private int selectedPageB = -1;
    
    private static final int BTN_DONE = 0;
	private static final int BTN_NEXT_PAGE = 1;
	private static final int BTN_PREVIOUS_PAGE = 2;
	private static final int BTN_SIGN = 3;
	private static final int BTN_SAVE_BOOK = 6;
	private static final int BTN_LOAD_BOOK = 7;
	private static final int BTN_COPY_BOOK = 10;
	private static final int BTN_PASTE_BOOK = 11;
	private static final int BTN_CUT_MULTIPLE_PAGES = 14;
	private static final int BTN_SELECT_PAGE_A = 15;
	private static final int BTN_SELECT_PAGE_B = 16;
	private static final int BTN_COPY_SELECTED_PAGES = 17;
	private static final int BTN_PASTE_MULTIPLE_PAGES = 18;
	private static final int BTN_ADD_SIGNATURE_PAGES = 21;
	private static final int BTN_BLACK = 50;
	private static final int BTN_DARK_BLUE = 51;
	private static final int BTN_DARK_GREEN = 52;
	private static final int BTN_DARK_AQUA = 53;
	private static final int BTN_DARK_RED = 54;
	private static final int BTN_DARK_PURPLE = 55;
	private static final int BTN_GOLD = 56;
	private static final int BTN_GRAY = 57;
	private static final int BTN_DARK_GRAY = 58;
	private static final int BTN_BLUE = 59;
	private static final int BTN_GREEN = 60;
	private static final int BTN_AQUA = 61;
	private static final int BTN_RED = 62;
	private static final int BTN_LIGHT_PURPLE = 63;
	private static final int BTN_YELLOW = 64;
	private static final int BTN_WHITE = 65;
	private static final int BTN_OBFUSCATED = 66;
	private static final int BTN_BOLD = 67;
	private static final int BTN_STRIKETHROUGH = 68;
	private static final int BTN_UNDERLINE = 69;
	private static final int BTN_ITALIC = 70;
	private static final int BTN_RESET_FORMAT = 71;
	
	/** Note that these are in the same order as the format button IDs. 
	 * Subtract 50 from the ID and that's the index of that formatting code */
	private static final String[] FORMAT_CODES = {
		//Color codes
		"\u00a70", "\u00a71", "\u00a72", "\u00a73", "\u00a74", "\u00a75", "\u00a76", "\u00a77", 
		"\u00a78", "\u00a79", "\u00a7a", "\u00a7b", "\u00a7c", "\u00a7d", "\u00a7e", "\u00a7f", 
		//Formatting codes
		"\u00a7k", "\u00a7l", "\u00a7m", "\u00a7n", "\u00a7o", "\u00a7r"};
    
	private GuiButton btnPasteBook;
	private GuiButton btnCutMultiplePages;	
	private GuiButton btnSelectPageA;
	private GuiButton btnSelectPageB;
	private GuiButton btnCopySelectedPages;
	private GuiButton btnPasteMultiplePages;
	private GuiButton btnBlack;
	private GuiButton btnDarkBlue;
	private GuiButton btnDarkGreen;
	private GuiButton btnDarkAqua;
	private GuiButton btnDarkRed;
	private GuiButton btnDarkPurple;
	private GuiButton btnGold;
	private GuiButton btnGray;
	private GuiButton btnDarkGray;
	private GuiButton btnBlue;
	private GuiButton btnGreen;
	private GuiButton btnAqua;
	private GuiButton btnRed;
	private GuiButton btnLightPurple;
	private GuiButton btnYellow;
	private GuiButton btnWhite;
	private GuiButton btnObfuscated;
	private GuiButton btnBold;
	private GuiButton btnStrikethrough;
	private GuiButton btnUnderline;
	private GuiButton btnItalic;
	private GuiButton btnResetFormat;
    
    private GuiGhostBook.NextPageButton btnNextPage;
    private GuiGhostBook.NextPageButton btnPreviousPage;
    
    private GBook book = new GBook();
    private GBook bookClipboard;
    private List<String> pageClipboard;
    private boolean heldBookIsWritable = false;;
    private ItemStack mcBookObj;
    private FileHandler fileHandler = new FileHandler();
    
    
    public GuiGhostBook(GBook _bookClipboard, List<String> _pageClipboard) {
    	//Set up the clipboards
    	this.bookClipboard = _bookClipboard;
    	this.pageClipboard = _pageClipboard;
    	
    	//Load any text from the held book
    	this.mcBookObj = this.mc.thePlayer.getHeldItem();
    	if (this.mcBookObj.getItem().equals(Items.writable_book)){
    		this.heldBookIsWritable = true;
    	}
    	
    	this.book = new GBook();
    	this.book.pages.add(new GPage());
        if (this.mcBookObj.hasTagCompound()){
            NBTTagCompound nbttagcompound = this.mcBookObj.getTagCompound();
            NBTTagList bookPages = nbttagcompound.getTagList("pages", 8);

            if (bookPages != null){
            	List<String> pages = new ArrayList();
                for (int i=0; i<bookPages.tagCount(); i++){
                	pages.add(bookPages.getStringTagAt(i));
                }
                if (pages.size() > 0){
                	this.book.clear();
                	this.book.insertPages(0, pages);
                }
            }
            
            //Get the title and author if the book has been signed
            if (!heldBookIsWritable){
            	String s = nbttagcompound.getString("author");
                if (!StringUtils.isNullOrEmpty(s)){
                    this.book.author = s;
                }
                s = nbttagcompound.getString("title");
                if (!StringUtils.isNullOrEmpty(s)){
                    this.book.title = s;
                }
            }
        }
    	
        
    	/**
    	 *  $$$$$$$$$$$$$$$$$$$  DEBUGGING SUFF!! REMOVE BEFORE RELEASE!!!!!!!!!!!!!!!!!!!!
    	 */
        //System.out.println("@@@@@ Adding debug text and setting cursor position...");
        
    	//this.book.clear();
    	//this.book.pages.add(new GPage());
    	//\u00a7l
    	//this.book.addText(0, 0, 0, "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.", false);
    	//this.book.addText(0, 0, 0, "[Short] Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt", false);
        
        /*
        this.setBook(fileHandler.loadBook(new File(fileHandler.getDefaultPath(), "bookworm samples\\982_SpaceJack_The-Scriptorium.txt")));
    	this.book.cursorPage = 4;
    	this.book.cursorLine = 11;
    	this.book.cursorPosChars = 0;
    	*/
        
        /*
        this.book.clear();
        this.book.pages.add(new GPage());
        this.book.addText(0, 0, 0, "a", false);
        */
    	
    }
    
    
    /** Sets the display book's contents to that of inBook */
    public void setBook(GBook inBook){
    	this.book.clone(inBook);
    	this.book.cursorPage = 0;
    	this.book.cursorLine = 0;
    	this.book.cursorPosChars = 0;
    }
    
    
    /** Helper function for laying out the color buttons */
    public int getColorButX(int buttonNum){
    	int middle = this.width/2;
    	int leftMost = middle - 160;
    	return leftMost + 20 * (buttonNum-50);
    }
    
    
    /** Helper function for laying out the format buttons */
    public int getFormatButX(int buttonNum){
    	int middle = this.width/2;
    	int leftMost = middle - 100;
    	return leftMost + 20 * (buttonNum-66);
    }
    
    
    public void initGui(){
    	Keyboard.enableRepeatEvents(true);
    	
        int buttonWidth = 120;
        int buttonHeight = 20;
        int buttonSideOffset = 5;
        ScaledResolution scaledResolution = new ScaledResolution(this.mc, this.mc.displayWidth, this.mc.displayHeight);
  		int rightXPos = scaledResolution.getScaledWidth() - (buttonWidth + buttonSideOffset);

  		if (this.heldBookIsWritable){
            this.buttonList.add(new GuiButton(BTN_SIGN, 5, 95, buttonWidth, buttonHeight, "Sign"));
            this.buttonList.add(new GuiButton(BTN_DONE, 5, 115, buttonWidth, buttonHeight, "Done"));
        
            this.buttonList.add(this.btnCutMultiplePages = new GuiButton(BTN_CUT_MULTIPLE_PAGES, rightXPos, 90, buttonWidth, buttonHeight, "Cut This Page"));
            this.buttonList.add(this.btnPasteBook = new GuiButton(BTN_PASTE_BOOK, rightXPos, 25, buttonWidth, buttonHeight, "Paste Book"));
      		this.buttonList.add(this.btnPasteMultiplePages = new GuiButton(BTN_PASTE_MULTIPLE_PAGES, rightXPos, 110, buttonWidth, buttonHeight, "Paste Page"));
      		this.buttonList.add(new GuiButton(BTN_LOAD_BOOK, 5, 25, buttonWidth, buttonHeight, "Load Book"));
      		this.buttonList.add(new GuiButton(BTN_ADD_SIGNATURE_PAGES, 5, 60, buttonWidth, buttonHeight, "Add Signature Pages"));
      		
            //The horror...
            int colorButY = this.height - 40;
            int formatButY = this.height - 20;
            this.buttonList.add(this.btnBlack = new GuiButton(BTN_BLACK, getColorButX(BTN_BLACK), colorButY, 20, 20, "\u00a70A"));
            this.buttonList.add(this.btnDarkBlue = new GuiButton(BTN_DARK_BLUE, getColorButX(BTN_DARK_BLUE), colorButY, 20, 20, "\u00a71A"));
            this.buttonList.add(this.btnDarkGreen = new GuiButton(BTN_DARK_GREEN, getColorButX(BTN_DARK_GREEN), colorButY, 20, 20, "\u00a72A"));
            this.buttonList.add(this.btnDarkAqua = new GuiButton(BTN_DARK_AQUA, getColorButX(BTN_DARK_AQUA), colorButY, 20, 20, "\u00a73A"));
            this.buttonList.add(this.btnDarkRed = new GuiButton(BTN_DARK_RED, getColorButX(BTN_DARK_RED), colorButY, 20, 20, "\u00a74A"));
            this.buttonList.add(this.btnDarkPurple = new GuiButton(BTN_DARK_PURPLE, getColorButX(BTN_DARK_PURPLE), colorButY, 20, 20, "\u00a75A"));
            this.buttonList.add(this.btnGold = new GuiButton(BTN_GOLD, getColorButX(BTN_GOLD), colorButY, 20, 20, "\u00a76A"));
            this.buttonList.add(this.btnGray = new GuiButton(BTN_GRAY, getColorButX(BTN_GRAY), colorButY, 20, 20, "\u00a77A"));
            this.buttonList.add(this.btnDarkGray = new GuiButton(BTN_DARK_GRAY, getColorButX(BTN_DARK_GRAY), colorButY, 20, 20, "\u00a78A"));
            this.buttonList.add(this.btnBlue = new GuiButton(BTN_BLUE, getColorButX(BTN_BLUE), colorButY, 20, 20, "\u00a79A"));
            this.buttonList.add(this.btnGreen = new GuiButton(BTN_GREEN, getColorButX(BTN_GREEN), colorButY, 20, 20, "\u00a7aA"));
            this.buttonList.add(this.btnAqua = new GuiButton(BTN_AQUA, getColorButX(BTN_AQUA), colorButY, 20, 20, "\u00a7bA"));
            this.buttonList.add(this.btnRed = new GuiButton(BTN_RED, getColorButX(BTN_RED), colorButY, 20, 20, "\u00a7cA"));
            this.buttonList.add(this.btnLightPurple = new GuiButton(BTN_LIGHT_PURPLE, getColorButX(BTN_LIGHT_PURPLE), colorButY, 20, 20, "\u00a7dA"));
            this.buttonList.add(this.btnYellow = new GuiButton(BTN_YELLOW, getColorButX(BTN_YELLOW), colorButY, 20, 20, "\u00a7eA"));
            this.buttonList.add(this.btnWhite = new GuiButton(BTN_WHITE, getColorButX(BTN_WHITE), colorButY, 20, 20, "\u00a7fA"));
            this.buttonList.add(this.btnObfuscated = new GuiButton(BTN_OBFUSCATED, getFormatButX(BTN_OBFUSCATED), formatButY, 20, 20, "#"));
            this.buttonList.add(this.btnBold = new GuiButton(BTN_BOLD, getFormatButX(BTN_BOLD), formatButY, 20, 20, "\u00a7lB"));
            this.buttonList.add(this.btnStrikethrough = new GuiButton(BTN_STRIKETHROUGH, getFormatButX(BTN_STRIKETHROUGH), formatButY, 20, 20, "\u00a7mS"));
            this.buttonList.add(this.btnUnderline = new GuiButton(BTN_UNDERLINE, getFormatButX(BTN_UNDERLINE), formatButY, 20, 20, "\u00a7nU"));
            this.buttonList.add(this.btnItalic = new GuiButton(BTN_ITALIC, getFormatButX(BTN_ITALIC), formatButY, 20, 20, "\u00a7oI"));
            this.buttonList.add(this.btnResetFormat = new GuiButton(BTN_RESET_FORMAT, getFormatButX(BTN_RESET_FORMAT), formatButY, 100, 20, "Reset Formatting"));
        }
        else{
            this.buttonList.add(new GuiButton(BTN_DONE, this.width / 2 - 100, 4 + this.bookImageHeight, 200, 20, "Done"));
        }
        
  		this.buttonList.add(new GuiButton(BTN_SAVE_BOOK, 5, 5, buttonWidth, buttonHeight, "Save Book"));
  		this.buttonList.add(new GuiButton(BTN_COPY_BOOK, rightXPos, 5, buttonWidth, buttonHeight, "Copy Book"));
  		this.buttonList.add(this.btnSelectPageA = new GuiButton(BTN_SELECT_PAGE_A, rightXPos, 50, buttonWidth/2, buttonHeight, "A"));
  		this.buttonList.add(this.btnSelectPageB = new GuiButton(BTN_SELECT_PAGE_B, rightXPos+buttonWidth/2, 50, buttonWidth/2, buttonHeight, "B"));
  		this.buttonList.add(this.btnCopySelectedPages = new GuiButton(BTN_COPY_SELECTED_PAGES, rightXPos, 70, buttonWidth, buttonHeight, "Copy This Page"));
  		
  		//Standard navigation buttons inside the book
  		int bookLeftSide = (this.width - this.bookImageWidth) / 2;
        this.buttonList.add(this.btnNextPage = new GuiGhostBook.NextPageButton(BTN_NEXT_PAGE, bookLeftSide + 120, 156, true));
        this.buttonList.add(this.btnPreviousPage = new GuiGhostBook.NextPageButton(BTN_PREVIOUS_PAGE, bookLeftSide + 38, 156, false));

    }
    
    /** Called when the screen is unloaded. Used to disable keyboard repeat events and send the book to the server. */
    public void onGuiClosed(){
        Keyboard.enableRepeatEvents(false);
        if (!this.book.isEmpty()){
        	this.book.sendBookToServer(false);
        }
    }
    
    /** Called when a button is clicked */
    protected void actionPerformed(GuiButton buttonPressed){
    	if (!buttonPressed.enabled){return;}
    	switch (buttonPressed.id){
			
			case BTN_NEXT_PAGE:
				this.book.turnPage(1);
				break;
				
			case BTN_PREVIOUS_PAGE:
				this.book.turnPage(-1);
				break;
				
    		case BTN_DONE:
    			this.mc.displayGuiScreen((GuiScreen)null);
    			//The book is automatically sent to the server when the gui is closed
    			break;
    			
    			
    		case BTN_SIGN:
    			if (this.heldBookIsWritable){
    				//Just in case...
    				this.book.title = GBook.truncateStringChars(this.book.title, "..", 16, false);
    				this.book.author = GBook.truncateStringChars(this.book.author, "..", 16, false);
    				this.mc.displayGuiScreen(new GuiSignGhostBook(this.book, this));
    			}
    			break;
			
    		case BTN_SAVE_BOOK:
    			fileHandler.saveBookToGHBFile(this.book);
    			break;
    			
    		case BTN_LOAD_BOOK:
    			this.mc.displayGuiScreen(new GuiFileBrowser(this));
    			break;
    			
    		case BTN_COPY_BOOK:
    			this.bookClipboard.clone(this.book);
    			break;
    			
    		case BTN_PASTE_BOOK:
    			if (this.bookClipboard != null){
	    			this.book.clone(this.bookClipboard);
    			}
    			else{
    				Printer.gamePrint(Printer.RED + "Clipboard is empty... Returning null...");
    			}
    			break;
    			
    		case BTN_SELECT_PAGE_A:
    			this.selectedPageA = this.book.cursorPage;
    			break;
    		
    		case BTN_SELECT_PAGE_B:
    			this.selectedPageB = this.book.cursorPage;
    			break;
    			
    		case BTN_COPY_SELECTED_PAGES:
    			if (this.selectedPageA != -1 && this.selectedPageB != -1 && this.selectedPageA >= 0 && 
    					this.selectedPageA <= this.selectedPageB && this.selectedPageB < this.book.pages.size()){
    				this.pageClipboard.clear();
    				this.pageClipboard.addAll(this.book.copyPages(this.selectedPageA, this.selectedPageB));
    			}
    			else{
    				this.pageClipboard.clear();
    				this.pageClipboard.addAll(this.book.copyPages(this.book.cursorPage, this.book.cursorPage));
    			}
    			break;
    			
    		case BTN_PASTE_MULTIPLE_PAGES:
    			this.book.insertPages(this.book.cursorPage, this.pageClipboard);
    			break;
    		
    		case BTN_CUT_MULTIPLE_PAGES:
    			if (this.selectedPageA != -1 && this.selectedPageB != -1 && this.selectedPageA >= 0 && 
				this.selectedPageA <= this.selectedPageB && this.selectedPageB < this.book.pages.size()){
    				this.pageClipboard.clear();
    				this.pageClipboard.addAll(this.book.cutPages(this.selectedPageA, this.selectedPageB));
				}
				else{
					this.pageClipboard.clear();
    				this.pageClipboard.addAll(this.pageClipboard = this.book.cutPages(this.book.cursorPage, this.book.cursorPage));
				}
				break;    		
    			
    		case BTN_ADD_SIGNATURE_PAGES:
    			File signatureFile = new File(fileHandler.getSignaturePath(), "default.ghb");
    			GBook signature = fileHandler.loadBook(signatureFile);
    			if (signature != null){
    				List<String> sigPages = new ArrayList();
    				for (GPage page : signature.pages){
    					sigPages.add(page.asString());
    				}
    				this.book.insertPages(this.book.totalPages(), sigPages);
    			}
    			else{
    				Printer.gamePrint(Printer.RED + "Signature file couldn't be loaded!");
    			}
    			break;
    			
    		default:
    			//Handle the formatting buttons
    	    	if (buttonPressed.id >= 50 && buttonPressed.id <= 71){
    	    		int pos = buttonPressed.id - 50;
    	    		this.book.addTextAtCursor((FORMAT_CODES[pos]));
    	    	}
    	    	else{
    	    		System.out.println("#################### BUTTON NOT CODED YET!!!!!!!!");
    	    	}
    			break;
    	}
    }
    

    /**
     * Called when the mouse is clicked.
     * posX and posY are the distance from the top-left corner of the window (measured in pixels)
     * button is 0 for a left click or 1 for a right click
     */
    protected void mouseClicked(int posX, int posY, int button)
    {
    	int bookLeftSide = (this.width - this.bookImageWidth) / 2;
    	
    	int bookTextLeft = bookLeftSide+36;
    	int bookTextTop = 33;
    	
    	if (posX >= bookTextLeft && posX < bookTextLeft+116 && posY >= bookTextTop && posY < bookTextTop+116){
    		//Figure out which line was clicked on
    		int rowGuess = (posY-bookTextTop)/9;
    		GPage currPage = this.book.pages.get(this.book.cursorPage);
    		if (rowGuess < 0){
    			rowGuess = 0;
    		}
    		else if (rowGuess > currPage.lines.size()-1){
    			rowGuess = currPage.lines.size()-1;
    		}
    		this.book.cursorLine = rowGuess;
    		
    		//Figure out where in the line was clicked
    		GLine currLine = currPage.lines.get(this.book.cursorLine);
    		int xOffset = posX-bookTextLeft;
    		if (xOffset < 0){xOffset = 0;}
    		int colGuess = GLine.sizeStringToApproxWidthBlind(currLine.getTextWithWrappedFormatting(), xOffset);
    		colGuess -= currLine.wrappedFormatting.length();
    		if (colGuess < 0){colGuess = 0;}
    		if (colGuess > 0 && currLine.text.charAt(colGuess-1) == '\n'){
    			colGuess--;
    		}
    		this.book.cursorPosChars = colGuess;
    	}
        
        super.mouseClicked(posX, posY, button);
    }
    
    
    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     */
    protected void keyTyped(char character, int keycode){
        switch (character){
	        case 22:
	        	//User pressed Ctrl+V (paste)
	            this.book.addTextAtCursor(GuiScreen.getClipboardString());
	            return;
	            
	        default:
		        switch (keycode){
		        
				    case Keyboard.KEY_LEFT:
				    	this.book.moveCursor(GBook.CursorDirection.LEFT);
				    	return;
				    	
				    case Keyboard.KEY_RIGHT:
				    	this.book.moveCursor(GBook.CursorDirection.RIGHT);
				    	return;
				    	
				    case Keyboard.KEY_UP:
				    	this.book.moveCursor(GBook.CursorDirection.UP);
				    	return;
				    	
				    case Keyboard.KEY_DOWN:
				    	this.book.moveCursor(GBook.CursorDirection.DOWN);
				    	return;
				    	
				    case Keyboard.KEY_BACK: //Backspace
				    	this.book.removeChar(false);
				    	return;
				    	
				    case Keyboard.KEY_DELETE:
				    	this.book.removeChar(true);
				    	return;
				    	
				    case Keyboard.KEY_RETURN:      //Enter
		            case Keyboard.KEY_NUMPADENTER: //Numpad enter
		                this.book.addTextAtCursor("\n");
		                return;
		                
		            case Keyboard.KEY_ESCAPE:
		            	this.mc.displayGuiScreen((GuiScreen)null);
		            	return;
				    	
				    default:
				    	if (ChatAllowedCharacters.isAllowedCharacter(character)){
				    		this.book.addTextAtCursor(Character.toString(character));
		                }
		        }
        }
    }
    
    
    /** Called from the main game loop to update the screen. */
    public void updateScreen(){
        super.updateScreen();
        ++this.updateCount;
    }
    
    
    /** Draws the screen and all the components in it. */
    public void drawScreen(int par1, int par2, float par3){
    	
    	//Enable/disable buttons
    	if (this.heldBookIsWritable){
	    	this.btnPasteBook.enabled = (!this.bookClipboard.pages.isEmpty());
	    	this.btnPasteMultiplePages.enabled = (!this.pageClipboard.isEmpty());
    	}
    	
    	//Change button text based on the pages selected and if the clipboard has anything in it
    	if (this.heldBookIsWritable){
	        if (this.btnPasteMultiplePages.enabled){
	        	this.btnPasteMultiplePages.displayString = "Paste " + this.pageClipboard.size() + " Page" + ((this.pageClipboard.size()==1)?"":"s");
	        }
	        else{
	        	this.btnPasteMultiplePages.displayString = "Paste Multiple";
	        }
    	}
        
    	if (this.selectedPageA >= this.book.totalPages() || this.selectedPageB >= this.book.totalPages()){
    		this.selectedPageA = -1;
    		this.selectedPageB = -1;
    	}
    	
    	if (this.selectedPageA != -1 && this.selectedPageB != -1 && this.selectedPageA >= 0 && 
    			this.selectedPageA <= this.selectedPageB && this.selectedPageB < this.book.totalPages()){
    		String xPages = ((this.selectedPageB-this.selectedPageA)+1) + " Page"  + ((this.selectedPageA!=this.selectedPageB)?"s":"");
    		this.btnCopySelectedPages.displayString = "Copy " + xPages;
    		if (this.heldBookIsWritable){this.btnCutMultiplePages.displayString = "Cut " + xPages;}
    		this.btnSelectPageA.displayString = "A: " + (this.selectedPageA+1);
    		this.btnSelectPageB.displayString = "B: " + (this.selectedPageB+1);
    	}
    	else if (this.selectedPageA != -1){
    		this.btnSelectPageA.displayString = "A: " + (this.selectedPageA+1);
    		this.btnCopySelectedPages.displayString = "Copy This Page";
    		if (this.heldBookIsWritable){this.btnCutMultiplePages.displayString = "Cut This Page";}
    	}
    	else if (this.selectedPageB != -1){
    		this.btnSelectPageB.displayString = "B: " + (this.selectedPageB+1);
    		this.btnCopySelectedPages.displayString = "Copy This Page";
    		if (this.heldBookIsWritable){this.btnCutMultiplePages.displayString = "Cut This Page";}
    	}
    	else{
    		this.btnCopySelectedPages.displayString = "Copy This Page";
    		if (this.heldBookIsWritable){this.btnCutMultiplePages.displayString = "Cut This Page";}
    		this.btnSelectPageA.displayString = "A";
    		this.btnSelectPageB.displayString = "B";
    	}
    	
    	//Draw the book background image
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(bookGuiTextures);
        int bookLeftSide = (this.width - this.bookImageWidth) / 2;
        byte b0 = 2;
        this.drawTexturedModalRect(bookLeftSide, b0, 0, 0, this.bookImageWidth, this.bookImageHeight);
        
        //Add the page text
        String currPageText = this.book.getCurrPageAsMCString();
        this.fontRendererObj.drawSplitString(currPageText, bookLeftSide + 36, b0 + 16 + 16, 116, 0);
        
        //Add the page indicator (page number and page count)
        String pageIndicator = String.format(PAGE_INDICATOR_TEMPLATE, book.cursorPage+1, book.totalPages());
        int pageIndicatorWidth = this.fontRendererObj.getStringWidth(pageIndicator);
        this.fontRendererObj.drawString(pageIndicator, bookLeftSide - pageIndicatorWidth + this.bookImageWidth - 44, b0 + 16, 0);
        
        //Draw the cursor
        int cursorX1 = bookLeftSide + 35 + this.book.getCursorX();
    	int cursorX2 = cursorX1 + 1;
    	int cursorY1 = 33 + (9*this.book.cursorLine);
    	int cursorY2 = cursorY1+9;
    	byte phase = (byte)(this.updateCount / 10 % 2);
    	int cursorColor = 0xFF000000;
    	if (phase == 1){
    		cursorColor = 0xFF9A9A9A;
    	}
    	drawRect(cursorX1, cursorY1, cursorX2, cursorY2, cursorColor);
        
        super.drawScreen(par1, par2, par3);
    }

    /** Next and last page buttons */
    static class NextPageButton extends GuiButton{
        private final boolean isRightArrow;

        public NextPageButton(int par1, int par2, int par3, boolean par4){
            super(par1, par2, par3, 23, 13, "");
            this.isRightArrow = par4;
            
        }
        
        public void drawButton(Minecraft mc, int p_146112_2_, int p_146112_3_){
            if (this.visible){
                boolean flag = p_146112_2_ >= this.xPosition && p_146112_3_ >= this.yPosition && p_146112_2_ < this.xPosition + this.width && p_146112_3_ < this.yPosition + this.height;
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                mc.getTextureManager().bindTexture(GuiGhostBook.bookGuiTextures);
                int k = 0;
                int l = 192;
                if (flag){k += 23;}
                if (!this.isRightArrow){l += 13;}
                this.drawTexturedModalRect(this.xPosition, this.yPosition, k, l, 23, 13);
            }
        }
    }
}