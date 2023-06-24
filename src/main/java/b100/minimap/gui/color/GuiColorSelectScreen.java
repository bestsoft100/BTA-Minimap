package b100.minimap.gui.color;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_CLAMP;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;

import java.awt.Color;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import b100.minimap.Minimap;
import b100.minimap.gui.CancelEventException;
import b100.minimap.gui.GuiButtonNavigation;
import b100.minimap.gui.GuiContainerBox;
import b100.minimap.gui.GuiElement;
import b100.minimap.gui.GuiNavigationContainer;
import b100.minimap.gui.GuiNavigationContainer.Position;
import b100.minimap.gui.GuiScreen;
import b100.minimap.gui.GuiTextComponent;
import b100.minimap.gui.GuiTextComponentInteger;
import b100.minimap.gui.GuiTextElement;
import b100.minimap.gui.GuiTextElement.Align;
import b100.minimap.gui.GuiTextField;
import b100.minimap.gui.TextComponentListener;
import b100.minimap.utils.Utils;
import net.minecraft.src.GLAllocation;

public class GuiColorSelectScreen extends GuiScreen implements TextComponentListener {
	
	protected int colorPickerTexture1;
	protected int colorPickerTexture2;
	
	protected GuiColorBrightnessSaturationElement1 colorElement1;
	protected GuiColorHueElement colorElement2;
	protected GuiColorPreviewElement colorPreviewElement;
	
	protected final int previousColor;
	protected int color;
	protected float[] hsb = new float[3];
	
	public GuiContainerBox container;
	
	public GuiNavigationContainer navTop;
	public GuiNavigationContainer navBottom;
	
	public List<ColorListener> colorListeners = new ArrayList<>();
	
	public String title = "Choose Color";
	
	public GuiTextElement textRed;
	public GuiTextElement textGreen;
	public GuiTextElement textBlue;
	
	public GuiTextElement textHue;
	public GuiTextElement textSat;
	public GuiTextElement textVal;
	
	public GuiTextComponentInteger textComponentRed;
	public GuiTextComponentInteger textComponentGreen;
	public GuiTextComponentInteger textComponentBlue;
	
	public GuiTextComponentInteger textComponentHue;
	public GuiTextComponentInteger textComponentSat;
	public GuiTextComponentInteger textComponentVal;
	
	public GuiTextField inputRed;
	public GuiTextField inputGreen;
	public GuiTextField inputBlue;
	
	public GuiTextField inputHue;
	public GuiTextField inputSat;
	public GuiTextField inputVal;
	
	private boolean updatingColor = false;
	
	public GuiColorSelectScreen(GuiScreen parentScreen, int color, ColorListener colorListener) {
		super(parentScreen);
		this.previousColor = this.color = color;
		
		colorListeners.add(colorListener);
	}
	
	public void setColor(int color, boolean updateRgbInput, boolean updateHsvInput) {
		updatingColor = true;
//		Minimap.log("Set Color: " + Integer.toHexString(color));
		
		int r = color >> 16 & 0xFF;
		int g = color >>  8 & 0xFF;
		int b = color >>  0 & 0xFF;
		
		Color.RGBtoHSB(r, g, b, hsb);
		
		this.color = color;

		if(updateRgbInput) {
			textComponentRed.setValue(r);
			textComponentGreen.setValue(g);
			textComponentBlue.setValue(b);
		}
		if(updateHsvInput) {
			textComponentHue.setValue((int) (hsb[0] * 360));
			textComponentSat.setValue((int) (hsb[1] * 100));
			textComponentVal.setValue((int) (hsb[2] * 100));
		}
		onUpdate();
		updatingColor = false;
	}

	@Override
	public void onInit() {
		container = add(new GuiContainerBox());
		
		colorElement1 = add(new GuiColorBrightnessSaturationElement1(this));
		colorElement2 = add(new GuiColorHueElement(this));
		colorPreviewElement = add(new GuiColorPreviewElement(this));
		
		navTop = add(new GuiNavigationContainer(this, container, Position.TOP));
		navBottom = add(new GuiNavigationContainer(this, container, Position.BOTTOM));

		navTop.add(new GuiButtonNavigation(this, title, container));
		navBottom.add(new GuiButtonNavigation(this, "Cancel", container).addActionListener((e) -> back()));
		navBottom.add(new GuiButtonNavigation(this, "OK", container).addActionListener((e) -> ok()));
		
		textRed = add(new GuiTextElement("R", Align.CENTER, Align.CENTER));
		textGreen = add(new GuiTextElement("G", Align.CENTER, Align.CENTER));
		textBlue = add(new GuiTextElement("B", Align.CENTER, Align.CENTER));
		
		textHue = add(new GuiTextElement("H", Align.CENTER, Align.CENTER));
		textSat = add(new GuiTextElement("S", Align.CENTER, Align.CENTER));
		textVal = add(new GuiTextElement("V", Align.CENTER, Align.CENTER));
		
		textComponentRed = (GuiTextComponentInteger) new GuiTextComponentInteger(0, 0, 255).addTextComponentListener(this);
		textComponentGreen = (GuiTextComponentInteger) new GuiTextComponentInteger(0, 0, 255).addTextComponentListener(this);
		textComponentBlue = (GuiTextComponentInteger) new GuiTextComponentInteger(0, 0, 255).addTextComponentListener(this);
		
		textComponentHue = (GuiTextComponentInteger) new GuiTextComponentInteger(0, 0, 360).addTextComponentListener(this);
		textComponentSat = (GuiTextComponentInteger) new GuiTextComponentInteger(0, 0, 100).addTextComponentListener(this);
		textComponentVal = (GuiTextComponentInteger) new GuiTextComponentInteger(0, 0, 100).addTextComponentListener(this);
		
		inputRed = add(new GuiTextField(this, textComponentRed));
		inputGreen = add(new GuiTextField(this, textComponentGreen));
		inputBlue = add(new GuiTextField(this, textComponentBlue));
		
		inputHue = add(new GuiTextField(this, textComponentHue));
		inputSat = add(new GuiTextField(this, textComponentSat));
		inputVal = add(new GuiTextField(this, textComponentVal));
		
		setColor(this.color, true, true);
	}
	
	@Override
	public void onResize() {
		final int paddingOuter = 3;
		final int paddingInner = 3;

		int size1 = 128;	// Width and height of the brightness and saturation picker, and also the area right of the hue picker
		int size2 = 8;		// Width of the hue picker
		int size3 = 32;		// Width and height of the preview element
		
		int innerWidth = size1 * 2 + size2 + 2 * paddingInner;
		int innerHeight = size1;
		
		int width = innerWidth + 2 * paddingOuter;
		int height = innerHeight + 2 * paddingOuter;
		
		container.setPosition((this.width - width) / 2, (this.height - height) / 2).setSize(width, height);
		
		int innerPosX = container.posX + paddingOuter;
		int innerPosY = container.posY + paddingOuter;
		
		colorElement1.setPosition(innerPosX, innerPosY).setSize(size1, size1);
		colorElement2.setPosition(innerPosX + size1 + paddingInner, innerPosY).setSize(size2, size1);
		
		int x1 = innerPosX + size1 + size2 + 2 * paddingInner;	// X Position of the right area
		int y1 = innerPosY + size3 + paddingInner;				// Y Position below the color preview
		
		colorPreviewElement.setPosition(x1 + (size1 - size3) / 2, innerPosY).setSize(size3, size3);
		
		int lineHeight = 10;
		int lineHeightPad = lineHeight + 1;
		int w1 = size1 - lineHeight - paddingInner; // Text field width
		
		textRed.setPosition(x1, y1 + 0 * lineHeightPad).setSize(lineHeight, lineHeight);
		textGreen.setPosition(x1, y1 + 1 * lineHeightPad).setSize(lineHeight, lineHeight);
		textBlue.setPosition(x1, y1 + 2 * lineHeightPad).setSize(lineHeight, lineHeight);
		
		inputRed.setPosition(x1 + lineHeight + paddingInner, y1 + 0 * lineHeightPad).setSize(w1, lineHeight);
		inputGreen.setPosition(x1 + lineHeight + paddingInner, y1 + 1 * lineHeightPad).setSize(w1, lineHeight);
		inputBlue.setPosition(x1 + lineHeight + paddingInner, y1 + 2 * lineHeightPad).setSize(w1, lineHeight);
		
		textHue.setPosition(x1, y1 + 4 * lineHeightPad).setSize(lineHeight, lineHeight);
		textSat.setPosition(x1, y1 + 5 * lineHeightPad).setSize(lineHeight, lineHeight);
		textVal.setPosition(x1, y1 + 6 * lineHeightPad).setSize(lineHeight, lineHeight);
		
		inputHue.setPosition(x1 + lineHeight + paddingInner, y1 + 4 * lineHeightPad).setSize(w1, lineHeight);
		inputSat.setPosition(x1 + lineHeight + paddingInner, y1 + 5 * lineHeightPad).setSize(w1, lineHeight);
		inputVal.setPosition(x1 + lineHeight + paddingInner, y1 + 6 * lineHeightPad).setSize(w1, lineHeight);
		
		super.onResize();
	}
	
	public void ok() {
		for(int i=0; i < colorListeners.size(); i++) {
			try{
				colorListeners.get(i).onColorChanged(this, color);
			}catch (CancelEventException e) {}
		}
		back();
	}
	
	@Override
	public void onGuiOpened() {
		createTextures();
	}
	
	@Override
	public void onGuiClosed() {
		deleteTextures();
	}
	
	public void onUpdate() {
		if(colorPickerTexture1 == 0 || colorPickerTexture2 == 0) {
			Minimap.log("Cannot update gradient texture because texture doesnt exist!");
			return;
		}
		
		final int res = 256;
		final float resMinus1 = res - 1;
		final int size1 = res * res * 4;
		final int size2 = 256 * 4;
		final int sizeTotal = size1 + size2;
		ByteBuffer buffer = Minimap.instance.minecraftHelper.getBufferWithCapacity(sizeTotal);
		
		buffer.position(0).limit(size1);
		float hue = hsb[0];
		for(int i=0; i < res; i++) {
			float brightness = 1.0f - i / resMinus1;
			for(int j=0; j < res; j++) {
				float saturation = j / resMinus1;
				
				int color = Color.HSBtoRGB(hue, saturation, brightness);
				
				byte a = (byte) (color >> 24);
				byte r = (byte) (color >> 16);
				byte g = (byte) (color >>  8);
				byte b = (byte) (color >>  0);
				
				buffer.put(r);
				buffer.put(g);
				buffer.put(b);
				buffer.put(a);
			}
		}
		
		buffer.position(size1).limit(sizeTotal);
		for(int i=0; i < 256; i++) {
			int color = Color.HSBtoRGB(1.0f - i / 255.0f, 1.0f, 1.0f);
			
			byte a = (byte) (color >> 24);
			byte r = (byte) (color >> 16);
			byte g = (byte) (color >>  8);
			byte b = (byte) (color >>  0);
			
			buffer.put(r);
			buffer.put(g);
			buffer.put(b);
			buffer.put(a);
		}
		
		boolean filter = false;
		int filterMode = filter ? GL_LINEAR : GL_NEAREST;
		
		buffer.position(0).limit(size1);
		glBindTexture(GL_TEXTURE_2D, colorPickerTexture1);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, res, res, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filterMode);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filterMode);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);
		
		buffer.position(size1).limit(sizeTotal);
		glBindTexture(GL_TEXTURE_2D, colorPickerTexture2);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, 1, 256, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filterMode);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filterMode);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);
	}
	
	public void createTextures() {
		if(colorPickerTexture1 == 0) colorPickerTexture1 = GLAllocation.generateTexture();
		if(colorPickerTexture2 == 0) colorPickerTexture2 = GLAllocation.generateTexture();
	}
	
	public void deleteTextures() {
		if(colorPickerTexture1 != 0) {
			glDeleteTextures(colorPickerTexture1);
			GLAllocation.textureNames.remove((Integer) colorPickerTexture1);
			colorPickerTexture1 = 0;
		}
		if(colorPickerTexture2 != 0) {
			glDeleteTextures(colorPickerTexture2);
			GLAllocation.textureNames.remove((Integer) colorPickerTexture2);
			colorPickerTexture2 = 0;
		}
	}
	
	public static abstract class GuiColorElement extends GuiElement {

		public GuiColorSelectScreen screen;
		
		public GuiColorElement(GuiColorSelectScreen screen) {
			this.screen = screen;
		}
		
		protected boolean dragging = false;
		
		@Override
		public void draw(float partialTicks) {
			glEnable(GL_TEXTURE_2D);
			glDisable(GL_BLEND);
			glBindTexture(GL_TEXTURE_2D, getTexture());
			utils.drawTexturedRectangle(posX, posY, width, height, 0.0f, 0.0f, 1.0f, 1.0f, 0xFFFFFFFF);
			glEnable(GL_BLEND);
			
			if(dragging) {
				updateColor(screen.cursorX, screen.cursorY);	
			}
		}
		
		@Override
		public void mouseEvent(int button, boolean pressed, int mouseX, int mouseY) {
			if(pressed && screen.getClickElementAt(mouseX, mouseY) == this) {
				dragging = true;
				updateColor(mouseX, mouseY);
			}
			if(!pressed && dragging) {
				dragging = false;
				updateColor(mouseX, mouseY);
			}
		}
		
		public abstract int getTexture();
		
		public abstract void updateColor(int mouseX, int mouseY);
		
		
	}
	
	public static class GuiColorBrightnessSaturationElement1 extends GuiColorElement {

		public GuiColorBrightnessSaturationElement1(GuiColorSelectScreen screen) {
			super(screen);
		}

		@Override
		public int getTexture() {
			return screen.colorPickerTexture1;
		}

		@Override
		public void updateColor(int mouseX, int mouseY) {
			float hue = screen.hsb[0];
			float sat = Utils.clamp((mouseX - posX) / (float) width, 0.0f, 1.0f);
			float val = 1.0f - Utils.clamp((mouseY - posY) / (float) height, 0.0f, 1.0f);
			screen.setColor(Color.HSBtoRGB(hue, sat, val), true, true);
		}
		
		@Override
		public void draw(float partialTicks) {
			super.draw(partialTicks);
		}
		
	}
	
	public static class GuiColorHueElement extends GuiColorElement {
		
		public GuiColorHueElement(GuiColorSelectScreen screen) {
			super(screen);
		}

		@Override
		public int getTexture() {
			return screen.colorPickerTexture2;
		}

		@Override
		public void updateColor(int mouseX, int mouseY) {
			float hue = 1.0f - Utils.clamp((mouseY - posY) / (float) height, 0.0f, 1.0f);
			float sat = screen.hsb[1];
			float val = screen.hsb[2];
			screen.setColor(Color.HSBtoRGB(hue, sat, val), true, true);
		}
		
	}
	
	public static class GuiColorPreviewElement extends GuiElement {

		public GuiColorSelectScreen screen;
		
		public GuiColorPreviewElement(GuiColorSelectScreen screen) {
			this.screen = screen;
		}
		
		@Override
		public void draw(float partialTicks) {
			int heightHalf = height / 2;
			glDisable(GL_TEXTURE_2D);
			utils.drawRectangle(posX, posY, width, heightHalf, screen.color | 0xFF000000);
			utils.drawRectangle(posX, posY + heightHalf, width, height - heightHalf, screen.previousColor);
		}
		
	}

	@Override
	public void onTextComponentChanged(GuiTextComponent textComponent) {
		if(updatingColor) {
			return;
		}
		if(textComponent == textComponentRed || textComponent == textComponentGreen || textComponent == textComponentBlue) {
			Minimap.log("RGB Changed");
			
			int r = textComponentRed.getValue() & 0xFF;
			int g = textComponentGreen.getValue() & 0xFF;
			int b = textComponentBlue.getValue() & 0xFF;
			
			setColor(r << 16 | g << 8 | b, false, true);
		}
		if(textComponent == textComponentHue || textComponent == textComponentSat || textComponent == textComponentVal) {
			Minimap.log("HSV Changed");
			
			float h = textComponentHue.getValue() / 360.0f;
			float s = textComponentSat.getValue() / 100.0f;
			float v = textComponentVal.getValue() / 100.0f;
			
			setColor(Color.HSBtoRGB(h, s, v), true, false);
		}
		
	}

}
