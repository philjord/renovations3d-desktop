/*
 * Label3D.java 7 avr. 2015
 *
 * Sweet Home 3D, Copyright (c) 2024 Space Mushrooms <info@sweethome3d.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.eteks.sweethome3d.j3d;

import org.jogamp.java3d.Appearance;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.GeometryArray;
import org.jogamp.java3d.Group;
import org.jogamp.java3d.Node;
import org.jogamp.java3d.PolygonAttributes;
import org.jogamp.java3d.RenderingAttributes;
import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.TexCoordGeneration;
import org.jogamp.java3d.Texture;
import org.jogamp.java3d.TextureAttributes;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.TransparencyAttributes;
import org.jogamp.java3d.utils.geometry.Box;
import org.jogamp.java3d.utils.image.TextureLoader;
import org.jogamp.java3d.utils.shader.SimpleShaderAppearance;
import org.jogamp.vecmath.Vector3d;
import org.jogamp.vecmath.Vector4f;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.Label;
import com.eteks.sweethome3d.model.TextStyle;

import javaawt.BasicStroke;
import javaawt.Color;
import javaawt.Font;
import javaawt.Graphics2D;
import javaawt.RenderingHints;
import javaawt.VMFont;
import javaawt.geom.Rectangle2D;
import javaawt.image.BufferedImage;

/**
 * Root of a label branch.
 * @author Emmanuel Puybaret
 */
public class Label3D extends Object3DBranch {
  public static final TransparencyAttributes DEFAULT_TRANSPARENCY_ATTRIBUTES = 
      new TransparencyAttributes(TransparencyAttributes.NICEST, 0);
  private static final PolygonAttributes      DEFAULT_POLYGON_ATTRIBUTES = 
      new PolygonAttributes(PolygonAttributes.POLYGON_FILL, PolygonAttributes.CULL_NONE, 0, false);
  private static final TextureAttributes MODULATE_TEXTURE_ATTRIBUTES = new TextureAttributes();
  
  static {
    MODULATE_TEXTURE_ATTRIBUTES.setTextureMode(TextureAttributes.MODULATE);
    MODULATE_TEXTURE_ATTRIBUTES.setCapability(TextureAttributes.ALLOW_TRANSFORM_READ);
    
    DEFAULT_POLYGON_ATTRIBUTES.setCapability(PolygonAttributes.ALLOW_MODE_READ);
    DEFAULT_POLYGON_ATTRIBUTES.setCapability(PolygonAttributes.ALLOW_CULL_FACE_READ);
    DEFAULT_POLYGON_ATTRIBUTES.setCapability(PolygonAttributes.ALLOW_NORMAL_FLIP_READ);
    
    DEFAULT_TRANSPARENCY_ATTRIBUTES.setCapability(TransparencyAttributes.ALLOW_VALUE_READ);
  }

  private String      text;
  private TextStyle   style;
  private Integer     color;
  private Transform3D baseLineTransform;
  private Texture     texture;
  
  public Label3D(Label label, Home home, boolean waitForLoading) {
    setUserData(label);

    // Allow piece branch to be removed from its parent
    setCapability(BranchGroup.ALLOW_DETACH);
    setCapability(BranchGroup.ALLOW_CHILDREN_READ);
    setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
    setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
    
    update();
    
    //selection
    setPickable(true);
    setCapability(Node.ENABLE_PICK_REPORTING);
  }

  @Override
  public void update() {
    Label label = (Label)getUserData();
    Float pitch = label.getPitch();
    TextStyle style = label.getStyle();
    if (pitch != null
        && style != null
        && (label.getLevel() == null
            || label.getLevel().isViewableAndVisible())) {
      String text = label.getText();
      Integer color = label.getColor();
      Integer outlineColor = label.getOutlineColor();
      if (!text.equals(this.text)
          || (style == null && this.style != null)
          || (style != null && !style.equals(this.style))
          || (color == null && this.color != null)
          || (color != null && !color.equals(this.color))) {
        // If text, style and color changed, recompute label texture            
       /* int fontStyle = Font.PLAIN;
        if (style.isBold()) {
          fontStyle = Font.BOLD;
        }
        if (style.isItalic()) {
        fontStyle |= Font.ITALIC;
        }
        Font defaultFont; 
        if (style.getFontName() != null) {
          defaultFont = new VMFont(getFont(), 12);//new Font(style.getFontName(), fontStyle, 1);
        } else {
          defaultFont = new VMFont(Typeface.DEFAULT, 12);//UIManager.getFont("TextField.font");
        }*/
        BasicStroke stroke = new BasicStroke(outlineColor != null ? style.getFontSize() * 0.05f : 0f); 
        
        BufferedImage dummyImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2D = (Graphics2D)dummyImage.getGraphics();        
        g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2D.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        //FontMetrics fontMetrics = g2D.getFontMetrics(font);
        Font font = g2D.getFont();// we get the system default font 
        font.setSize((int) style.getFontSize());
       
        float lineHeight = (float)font.getStringBounds("[").getHeight() * 1.1f; //force a line gap, cos getHeight on Android is a bit small
        String [] lines = text.split("\n");
        float [] lineWidths = new float [lines.length];
        float textWidth = -Float.MAX_VALUE;
        float baseLineShift = 0;
        for (int i = 0; i < lines.length; i++) {
          Rectangle2D lineBounds = font.getStringBounds(lines [i]);
          if (i == 0) {
            baseLineShift = -(float)lineBounds.getY() + lineHeight * (lines.length - 1);
          }
          lineWidths [i] = (float)lineBounds.getWidth() + 2 * stroke.getLineWidth();
          if (style.isItalic()) {
          //  lineWidths [i] += fontMetrics.getAscent() * 0.2;
          }
          textWidth = Math.max(lineWidths [i], textWidth);
        }
        g2D.dispose();
        
        float textHeight = lineHeight * lines.length + 2 * stroke.getLineWidth();
        float textRatio = (float)Math.sqrt((float)textWidth / textHeight);
        int width;
        int height;
        float scale;
        // Ensure that text image size is between 256x256 and 512x512 pixels
        if (textRatio > 1) {
          width = (int)Math.ceil(Math.max(255 * textRatio, Math.min(textWidth, 511 * textRatio)));
          scale = (float)(width / textWidth);
          height = (int)Math.ceil(scale * textHeight);
        } else {
          height = (int)Math.ceil(Math.max(255 * textRatio, Math.min(textHeight, 511 / textRatio)));
          scale = (float)(height / textHeight);
          width = (int)Math.ceil(scale * textWidth);
        }
  
        if (width > 0 && height > 0) {
          // Draw text in an image
          BufferedImage textureImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);        
          g2D = (Graphics2D)textureImage.getGraphics();
          g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
          g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
          g2D.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
          // PJ affines are used as save/restore points in android g2D.setTransform(AffineTransform.getScaleInstance(scale, scale));
          g2D.scale(scale, scale);
          g2D.translate(0, baseLineShift);
          for (int i = lines.length - 1; i >= 0; i--) {
            String line = lines [i];
            float translationX;
            if (style.getAlignment() == TextStyle.Alignment.LEFT) {
              translationX = 0;
            } else if (style.getAlignment() == TextStyle.Alignment.RIGHT) {
              translationX = textWidth - lineWidths [i];
            } else { // CENTER
              translationX = (textWidth - lineWidths [i]) / 2;
            }
            translationX += stroke.getLineWidth() / 2;
            g2D.translate(translationX, 0);
            if (outlineColor != null) {
              g2D.setColor(new Color(outlineColor));
              g2D.setStroke(stroke);
              if (line.length() > 0) {
                //TextLayout textLayout = new TextLayout(line, font, g2D.getFontRenderContext());
                //g2D.draw(textLayout.getOutline(null));
              }
            }
            g2D.setFont(font);
            //g2D.setColor(color != null ?  new Color(color) : new Color(0xff000000));//UIManager.getColor("TextField.foreground"));
            //FIXME: my bit shifting on the setColor above is wrong somehow?, but setPaint gets it right
            g2D.setPaint(color != null ?  new Color(color) : new Color(0xff000000));
            g2D.drawString(line, 0f, 0f);
            g2D.translate(-translationX, -lineHeight);
          }
          g2D.dispose();
          
          Transform3D scaleTransform = new Transform3D();
          scaleTransform.setScale(new Vector3d(textWidth, 1, textHeight));          
          // Move to the middle of base line
          this.baseLineTransform = new Transform3D();
          float translationX;
          if (style.getAlignment() == TextStyle.Alignment.LEFT) {
            translationX = textWidth / 2;
          } else if (style.getAlignment() == TextStyle.Alignment.RIGHT) {
            translationX = -textWidth / 2;
          } else { // CENTER
            translationX = 0;
          }
          this.baseLineTransform.setTranslation(new Vector3d(translationX, 0, textHeight / 2 - baseLineShift));
          this.baseLineTransform.mul(scaleTransform);
          this.texture = new TextureLoader(textureImage).getTexture();
          this.text = text;
          this.style = style;
          this.color = color;
        } else {
          clear();
        }
      }
      
      if (this.texture != null) {
        if (numChildren() == 0) {
          BranchGroup group = new BranchGroup();
          group.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
          group.setCapability(BranchGroup.ALLOW_DETACH);
          
          group.setPickable(true);
          
          TransformGroup transformGroup = new TransformGroup();
          // Allow the change of the transformation that sets label size, position and orientation
          transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
          transformGroup.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
          group.addChild(transformGroup);
  
          transformGroup.setPickable(true);
          
          SimpleShaderAppearance appearance = new SimpleShaderAppearance();          
          appearance.setMaterial(getMaterial(DEFAULT_COLOR, DEFAULT_AMBIENT_COLOR, 0));
          appearance.setPolygonAttributes(DEFAULT_POLYGON_ATTRIBUTES);
          appearance.setTextureAttributes(MODULATE_TEXTURE_ATTRIBUTES);
          appearance.setTransparencyAttributes(DEFAULT_TRANSPARENCY_ATTRIBUTES);
          appearance.setTexCoordGeneration(new TexCoordGeneration(TexCoordGeneration.OBJECT_LINEAR,
              TexCoordGeneration.TEXTURE_COORDINATE_2, new Vector4f(1, 0, 0, .5f), new Vector4f(0, 1, -1, .5f)));
          appearance.setCapability(Appearance.ALLOW_TEXTURE_WRITE);
          appearance.setUpdatableCapabilities();
          
          // Do not share box geometry or cleaning up the universe after an offscreen rendering may cause some bugs
          Box box = new Box(0.5f, 0f, 0.5f, Box.GEOMETRY_NOT_SHARED | Box.GENERATE_NORMALS | Box.ENABLE_GEOMETRY_PICKING, appearance);
          Shape3D shape = box.getShape(Box.TOP);
          box.removeChild(shape);         
          makePickable(shape); //PJPJP for selection
          
          shape.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
		  if (!shape.getGeometry().isLive() && !shape.getGeometry().isCompiled())
		  {
			shape.getGeometry().setCapability(GeometryArray.ALLOW_NORMAL_READ);
		  }
                   
          // base shape outlining
          int outlineStencilMask = Object3DBranch.LABEL_STENCIL_MASK;
          RenderingAttributes renderingAttributes = new RenderingAttributes();
          renderingAttributes.setStencilEnable(false);
          renderingAttributes.setStencilWriteMask(outlineStencilMask);
          renderingAttributes.setStencilFunction(RenderingAttributes.ALWAYS, outlineStencilMask, outlineStencilMask);
          renderingAttributes.setStencilOp(RenderingAttributes.STENCIL_REPLACE, //
    				RenderingAttributes.STENCIL_REPLACE, //
    				RenderingAttributes.STENCIL_REPLACE);     
          renderingAttributes.setCapability(RenderingAttributes.ALLOW_STENCIL_ATTRIBUTES_WRITE);          
          appearance.setRenderingAttributes(renderingAttributes);
          appearance.setCapability(Appearance.ALLOW_RENDERING_ATTRIBUTES_READ);
          
          //Outlining                    
          RenderingAttributes olRenderingAttributes = new RenderingAttributes();          
          SimpleShaderAppearance outlineAppearance = new SimpleShaderAppearance(Object3DBranch.OUTLINE_COLOR);// special non auto build version for outlining
          outlineAppearance.setCapability(Appearance.ALLOW_RENDERING_ATTRIBUTES_READ);
          
          outlineAppearance.setColoringAttributes(Object3DBranch.OUTLINE_COLORING_ATTRIBUTES);
          outlineAppearance.setPolygonAttributes(Object3DBranch.OUTLINE_POLYGON_ATTRIBUTES);
          outlineAppearance.setLineAttributes(Object3DBranch.OUTLINE_LINE_ATTRIBUTES);
          
          outlineAppearance.setTransparencyAttributes(DEFAULT_TRANSPARENCY_ATTRIBUTES);//put it in the transparent pass
          olRenderingAttributes.setStencilEnable(true);
          olRenderingAttributes.setStencilWriteMask(outlineStencilMask);
          olRenderingAttributes.setStencilFunction(RenderingAttributes.NOT_EQUAL, outlineStencilMask, outlineStencilMask);
          olRenderingAttributes.setStencilOp(RenderingAttributes.STENCIL_KEEP, //
    				RenderingAttributes.STENCIL_KEEP, //
    				RenderingAttributes.STENCIL_KEEP);
    		//geoms often have colors in verts
          olRenderingAttributes.setIgnoreVertexColors(true);
    		// draw it even when hidden
          olRenderingAttributes.setDepthBufferEnable(false);
          olRenderingAttributes.setDepthTestFunction(RenderingAttributes.ALWAYS);	
          olRenderingAttributes.setVisible(false);

          olRenderingAttributes.setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);
          outlineAppearance.setRenderingAttributes(olRenderingAttributes);
          
          Shape3D olShape = new Shape3D();
          olShape.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
          olShape.setAppearance(outlineAppearance);
          olShape.setGeometry(shape.getGeometry());              

          transformGroup.addChild(shape);    
          transformGroup.addChild(olShape); // outline is child 1
          addChild(group);     
        }
        
        TransformGroup transformGroup = (TransformGroup)(((Group)getChild(0)).getChild(0));
        // Apply pitch rotation
        Transform3D pitchRotation = new Transform3D();
        pitchRotation.rotX(pitch);
        pitchRotation.mul(this.baseLineTransform);
        // Apply rotation around vertical axis
        Transform3D rotationY = new Transform3D();
        rotationY.rotY(-label.getAngle());
        rotationY.mul(pitchRotation);
        Transform3D transform = new Transform3D();
        transform.setTranslation(new Vector3d(label.getX(), label.getGroundElevation() + (pitch == 0f && label.getElevation() < 0.1f ? 0.1f : 0), label.getY()));
        transform.mul(rotationY);
        transformGroup.setTransform(transform);
        ((Shape3D)transformGroup.getChild(0)).getAppearance().setTexture(this.texture);        
        
      }
    } else {
      clear();
    }
  }

  /**
   * Removes children and clear fields. 
   */
  private void clear() {
    removeAllChildren();
    this.text  = null;
    this.style = null;
    this.color = null;
    this.texture = null;
    this.baseLineTransform = null;
  }
  
  	@Override
	public void showOutline(boolean isSelected)
	{
  		isShowOutline = isSelected;
  		// only if we haven't been cleared
  		if (numChildren() > 0)
  		{
	  		 TransformGroup transformGroup = (TransformGroup)(((Group)getChild(0)).getChild(0));
	         ((Shape3D)transformGroup.getChild(1)).getAppearance().getRenderingAttributes().setVisible(isSelected);
	         ((Shape3D)transformGroup.getChild(0)).getAppearance().getRenderingAttributes().setStencilEnable(isSelected);
	  	}
	}
	private boolean isShowOutline = false;
	@Override
	public boolean isShowOutline()
	{
		return isShowOutline;
	}
}
