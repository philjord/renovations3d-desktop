package com.eteks.sweethome3d.j3d.mouseover;

import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.Locale;
import org.jogamp.java3d.PickInfo;
import org.jogamp.java3d.utils.pickfast.PickCanvas;

import com.jogamp.newt.event.MouseAdapter;
import com.jogamp.newt.event.MouseEvent;

public abstract class MouseOverHandler {

	protected Canvas3D canvas3D;

	protected PickCanvas pickCanvas;

	protected MouseEvent lastMouseEvent;

	private MouseAdapter mouseAdapter = new MouseAdapter() {
		@Override
		public void mouseExited(MouseEvent e) {
			doMouseExited(e);
		}

		@Override
		public void mousePressed(MouseEvent e) {
			doMousePressed(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			doMouseReleased(e);
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			doMouseMoved(e);
		}

		@Override
		public void mouseClicked(final MouseEvent e) {
			doMouseClicked(e);
		}
	};

	public MouseOverHandler() {
	}
	
	public void doMouseMoved(MouseEvent e) {
		// record the mouse move for the picker to use when it next wakes up
		lastMouseEvent = e;
	}

	public void doMouseExited(MouseEvent e) {
		lastMouseEvent = null;
	}
	
	public abstract void doMouseClicked(MouseEvent e);

	public abstract void doMouseReleased(MouseEvent e);

	public abstract void doMousePressed(MouseEvent e);

	public void setConfig(Canvas3D canvas, Locale locale) {
		// de-register on the old canvas
		if (this.canvas3D != null) {
			canvas3D.getGLWindow().removeMouseListener(mouseAdapter);
		}

		// set up new canvas
		this.canvas3D = canvas;
		if (this.canvas3D != null) {
			pickCanvas = new PickCanvas(canvas3D, locale);
			pickCanvas.setMode(PickInfo.PICK_GEOMETRY);
			pickCanvas.setTolerance(0.0f);// make sure it's a ray not a cone

			canvas3D.getGLWindow().addMouseListener(mouseAdapter);
		}
	}
}
