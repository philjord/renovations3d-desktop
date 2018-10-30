/*
 * Polyline3D.java 11 sept. 2018
 *
 * Sweet Home 3D, Copyright (c) 2015 Emmanuel PUYBARET / eTeks <info@eteks.com>
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

import javaawt.BasicStroke;
import javaawt.Shape;
import javaawt.Stroke;
import javaawt.geom.AffineTransform;
import javaawt.geom.Area;
import javaawt.geom.Ellipse2D;
import javaawt.geom.GeneralPath;
import javaawt.geom.PathIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jogamp.java3d.Appearance;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.GeometryArray;
import org.jogamp.java3d.Group;
import org.jogamp.java3d.Node;
import org.jogamp.java3d.PolygonAttributes;
import org.jogamp.java3d.RenderingAttributes;
import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.utils.geometry.GeometryInfo;
import org.jogamp.java3d.utils.shader.SimpleShaderAppearance;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3d;
import org.jogamp.vecmath.Vector3f;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.Polyline;
 

/**
 * Root of a polyline branch.
 * @author Emmanuel Puybaret
 */
public class Polyline3D extends Object3DBranch {
  private static final PolygonAttributes  DEFAULT_POLYGON_ATTRIBUTES =
      new PolygonAttributes(PolygonAttributes.POLYGON_FILL, PolygonAttributes.CULL_NONE, 0, false);
  private static final GeneralPath ARROW;

  static {
    ARROW = new GeneralPath();
    ARROW.moveTo(-5, -2);
    ARROW.lineTo(0, 0);
    ARROW.lineTo(-5, 2);
  }

  public Polyline3D(Polyline polyline, Home home) {
    setUserData(polyline);

    // Allow branch to be removed from its parent
    setCapability(BranchGroup.ALLOW_DETACH);
    setCapability(BranchGroup.ALLOW_CHILDREN_READ);
    setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
    setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);

    update();
    
    //TODO: poly lines aren't pickable, and some sort of box is needed
    //selection
    setPickable(true);
    setCapability(Node.ENABLE_PICK_REPORTING);
  }

  @Override
  public void update() {
    Polyline polyline = (Polyline)getUserData();
    if (polyline.isVisibleIn3D()
        && (polyline.getLevel() == null
            || polyline.getLevel().isViewableAndVisible())) {
      Stroke stroke = ShapeTools.getStroke(polyline.getThickness(), polyline.getCapStyle(),
          polyline.getJoinStyle(), polyline.getDashPattern(), polyline.getDashOffset());
      Shape polylineShape = ShapeTools.getPolylineShape(polyline.getPoints(),
          polyline.getJoinStyle() == Polyline.JoinStyle.CURVED, polyline.isClosedPath());

      // Search angle at start and at end
      float [] firstPoint = null;
      float [] secondPoint = null;
      float [] beforeLastPoint = null;
      float [] lastPoint = null;
      for (PathIterator it = polylineShape.getPathIterator(null, 0.5); !it.isDone(); it.next()) {
        float [] pathPoint = new float [2];
        if (it.currentSegment(pathPoint) != PathIterator.SEG_CLOSE) {
          if (firstPoint == null) {
            firstPoint = pathPoint;
          } else if (secondPoint == null) {
            secondPoint = pathPoint;
          }
          beforeLastPoint = lastPoint;
          lastPoint = pathPoint;
        }
      }
      float angleAtStart = (float)Math.atan2(firstPoint [1] - secondPoint [1],
          firstPoint [0] - secondPoint [0]);
      float angleAtEnd = (float)Math.atan2(lastPoint [1] - beforeLastPoint [1],
          lastPoint [0] - beforeLastPoint [0]);
      float arrowDelta = polyline.getCapStyle() != Polyline.CapStyle.BUTT
          ? polyline.getThickness() / 2
          : 0;
      Shape [] polylineShapes = {getArrowShape(firstPoint, angleAtStart, polyline.getStartArrowStyle(), polyline.getThickness(), arrowDelta),
                                 getArrowShape(lastPoint, angleAtEnd, polyline.getEndArrowStyle(), polyline.getThickness(), arrowDelta),
                                 stroke.createStrokedShape(polylineShape)};
      Area polylineArea = new Area();
      for (Shape shape : polylineShapes) {
        if (shape != null) {
          polylineArea.add(new Area(shape));
        }
      }
      List<Point3f> vertices = new ArrayList<Point3f>(4);
      List<float [][]> polylinePoints = getAreaPoints(polylineArea, 0.5f, false);
      int [] stripCounts = new int [polylinePoints.size()];
      int currentShapeStartIndex = 0;
      for (int i = 0; i < polylinePoints.size(); i++) {
        for (float [] point : polylinePoints.get(i)) {
          vertices.add(new Point3f(point [0], 0, point [1]));
        }
        stripCounts [i] = vertices.size() - currentShapeStartIndex;
        currentShapeStartIndex = vertices.size();
      }

      GeometryInfo geometryInfo = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
      geometryInfo.setCoordinates(vertices.toArray(new Point3f [vertices.size()]));
      Vector3f [] normals = new Vector3f [vertices.size()];
      Arrays.fill(normals, new Vector3f(0, 1, 0));
      geometryInfo.setNormals(normals);
      geometryInfo.setStripCounts(stripCounts);
      GeometryArray geometryArray = geometryInfo.getIndexedGeometryArray(true, true, true, true, true);

      if (numChildren() == 0) {
        BranchGroup group = new BranchGroup();
        group.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
        group.setCapability(BranchGroup.ALLOW_DETACH);
        
        group.setPickable(true);

        TransformGroup transformGroup = new TransformGroup();
        // Allow the change of the transformation that sets polyline position and orientation
        transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        transformGroup.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
        group.addChild(transformGroup);
        
        transformGroup.setPickable(true);

        SimpleShaderAppearance appearance = new SimpleShaderAppearance();
        appearance.setMaterial(getMaterial(DEFAULT_COLOR, DEFAULT_AMBIENT_COLOR, 0));
        appearance.setCapability(Appearance.ALLOW_MATERIAL_WRITE);
        appearance.setPolygonAttributes(DEFAULT_POLYGON_ATTRIBUTES);
        appearance.setUpdatableCapabilities();

        Shape3D shape = new Shape3D(geometryArray, appearance);
        shape.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
        shape.setCapability(Shape3D.ALLOW_APPEARANCE_READ);

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
        
        outlineAppearance.setTransparencyAttributes(Label3D.DEFAULT_TRANSPARENCY_ATTRIBUTES);//put it in the transparent pass
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
        olShape.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
        olShape.setAppearance(outlineAppearance);
        olShape.setGeometry(geometryArray);              

        transformGroup.addChild(shape);    
        transformGroup.addChild(olShape); // outline is child 1
        addChild(group);
      } else {
        Shape3D shape = (Shape3D)((TransformGroup)(((Group)getChild(0)).getChild(0))).getChild(0);
        shape.setGeometry(geometryArray);
        
        TransformGroup transformGroup = (TransformGroup)(((Group)getChild(0)).getChild(0));
        ((Shape3D)transformGroup.getChild(1)).setGeometry(geometryArray);
      }

      TransformGroup transformGroup = (TransformGroup)(((Group)getChild(0)).getChild(0));
      // Apply elevation
      Transform3D transform = new Transform3D();
      transform.setTranslation(new Vector3d(0, polyline.getGroundElevation() + (polyline.getElevation() < 0.05f ? 0.05f : 0), 0));
      transformGroup.setTransform(transform);
      ((Shape3D)transformGroup.getChild(0)).getAppearance().setMaterial(getMaterial(polyline.getColor(), polyline.getColor(), 0));
    } else {
      removeAllChildren();
    }
    
    showOutline(isShowOutline);
    
  }

  /**
   * Returns the shape of polyline arrow at the given point and orientation.
   */
  private Shape getArrowShape(float [] point, float angle,
                              Polyline.ArrowStyle arrowStyle, float thickness, float arrowDelta) {
    if (arrowStyle != null
        && arrowStyle != Polyline.ArrowStyle.NONE) {
      AffineTransform transform = AffineTransform.getTranslateInstance(point [0], point [1]);
      transform.rotate(angle);
      transform.translate(arrowDelta, 0);
      double scale = Math.pow(thickness, 0.66f) * 2;
      transform.scale(scale, scale);
      GeneralPath arrowPath = new GeneralPath();
      switch (arrowStyle) {
        case DISC :
          arrowPath.append(new Ellipse2D.Float(-3.5f, -2, 4, 4), false);
          break;
        case OPEN :
          BasicStroke arrowStroke = new BasicStroke((float)(thickness / scale / 0.9), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
          arrowPath.append(arrowStroke.createStrokedShape(ARROW).getPathIterator(AffineTransform.getScaleInstance(0.9, 0.9), 0), false);
          break;
        case DELTA :
          GeneralPath deltaPath = new GeneralPath(ARROW);
          deltaPath.closePath();
          arrowPath.append(deltaPath.getPathIterator(AffineTransform.getTranslateInstance(1.65f, 0), 0), false);
          break;
        default:
          return null;
      }
      return arrowPath.createTransformedShape(transform);
    }
    return null;
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
