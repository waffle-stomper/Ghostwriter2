package wafflestomper.ghostwriter2.gui;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import wafflestomper.ghostwriter2.gbook.GBook;
import wafflestomper.ghostwriter2.gbook.GLine;
import wafflestomper.ghostwriter2.gbook.GPage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

public class GuiSignGhostBook extends GuiScreen{
	
    private int bookImageWidth = 192;
    private int bookImageHeight = 192;
    private static final ResourceLocation bookGuiTextures = new ResourceLocation("textures/gui/book.png");
    private int updateCount = 0;
    private boolean titleSelected = true;
    
	private static final int BTN_CANCEL = 4;
	private static final int BTN_FINALISE = 5;
	private GuiButton btnFinalise;
    
    private GBook book;
    private GuiGhostBook parentScreen;
    
    
    public GuiSignGhostBook(GBook _book, GuiGhostBook _parentScreen){
    	this.book = _book;
    	this.parentScreen = _parentScreen;
    }
    
    
    public void initGui(){ 
    	
        this.buttonList.add(btnFinalise = new GuiButton(BTN_FINALISE, this.width / 2 - 100, 4 + this.bookImageHeight, 98, 20, "Finalise"));
        this.buttonList.add(new GuiButton(BTN_CANCEL, this.width / 2 + 2, 4 + this.bookImageHeight, 98, 20, "Cancel"));
        
        updateButtons();
    }
    
    
    protected void actionPerformed(GuiButton buttonPressed){
    	if (!buttonPressed.enabled){return;}
    	switch (buttonPressed.id){
    			
    		case BTN_CANCEL:
    			goBackToParentScreen();
    			break;
    			
    		case BTN_FINALISE:
	            this.book.sendBookToServer(true);
	            this.mc.displayGuiScreen((GuiScreen)null);
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
    	
    	if (posX >= bookTextLeft && posX < bookTextLeft+116){
    		if (posY >= 45 && posY <= 54){
    			this.titleSelected = true;
    		}
    		else if (posY >= 69 && posY <= 78){
    			this.titleSelected = false;
    		}
    	}
        
        super.mouseClicked(posX, posY, button);
    }
    
    
    /** Displays the screen that spawned this one */
	private void goBackToParentScreen(){
		this.mc.displayGuiScreen(this.parentScreen);
	}
    
    
    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     */
    protected void keyTyped(char character, int keycode){
    	
    	switch (keycode){
    		case Keyboard.KEY_ESCAPE:
    			goBackToParentScreen();
            	return;
            
    		case Keyboard.KEY_RETURN:
    		case Keyboard.KEY_NUMPADENTER:
    		case Keyboard.KEY_TAB:
    			this.titleSelected = !this.titleSelected;
    			return;
    			
    		case Keyboard.KEY_BACK: //Backspace
    			if (this.titleSelected){
            		if (this.book.title.length() > 0){
            			this.book.title = this.book.title.substring(0, this.book.title.length()-1);
            		}
            	}
            	else if (this.book.author.length() > 0){
            		this.book.author = this.book.author.substring(0, this.book.author.length()-1);
            	}
    			return;
    	}
        
        if (ChatAllowedCharacters.isAllowedCharacter(character)){
        	if (this.titleSelected){
        		if (this.book.title.length() < 16){
        			this.book.title += character;
        		}
        	}
        	else if (this.book.author.length() < 16){
        		this.book.author += character;
        	}
        	
        }
        
        updateButtons();
    }
    
    private void updateButtons(){
    	if (this.book.title.isEmpty() || this.book.author.isEmpty()){
    		this.btnFinalise.enabled = false;
    	}
    	else{
    		this.btnFinalise.enabled = true;
    	}
    }
    
    
    /**
     * Called from the main game loop to update the screen.
     */
    public void updateScreen(){
        super.updateScreen();
        ++this.updateCount;
    }
    
    
    /**
     * Helper function for drawing the text components of the signing page
     */
    private void drawCenteredBookString(String str, int top, int colour){
    	int bookLeftSide = (this.width - this.bookImageWidth) / 2;
        int strWidth = this.fontRendererObj.getStringWidth(str);
        this.fontRendererObj.drawString(str, bookLeftSide + 36 + (116 - strWidth) / 2, top, colour);
    }
    
	
    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int par1, int par2, float par3){
    	    	
    	//Draw the book background image
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(bookGuiTextures);
        int bookLeftSide = (this.width - this.bookImageWidth) / 2;
        byte b0 = 2;
        this.drawTexturedModalRect(bookLeftSide, b0, 0, 0, this.bookImageWidth, this.bookImageHeight);
        
        //Draw the areas for the title and author
        String cursor = "";
    	/** Flashing cursor */
        if (this.updateCount / 10 % 2 == 0){
        	cursor = EnumChatFormatting.BLACK + "_";
        }
        else{
        	cursor = EnumChatFormatting.GRAY + "_";
        }
        
        String titleLine = this.book.title;
        String authorLine = this.book.author;
        
        if (this.titleSelected){
        	titleLine += cursor;
        }
        else{
        	authorLine += cursor;
        }
                    
        drawCenteredBookString("\u00A7lTitle:\u00A7r", 34, 0);
        drawCenteredBookString(titleLine, 46, 0);
        
        drawCenteredBookString("\u00A7lAuthor:\u00A7r", 58, 0);
        drawCenteredBookString(authorLine, 70, 0);
        
        
        String s3 = "Press tab to switch between the title and author fields.\n\n" + 
        			"Note! When you sign the book, it will no longer be editable.";
        this.fontRendererObj.drawSplitString(s3, bookLeftSide + 36, b0 + 90, 116, 0);
        
        super.drawScreen(par1, par2, par3);
    }
}
