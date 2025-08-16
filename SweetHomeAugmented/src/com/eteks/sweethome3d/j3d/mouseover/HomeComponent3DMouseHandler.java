package com.eteks.sweethome3d.j3d.mouseover;

import java.util.ArrayList;

import org.jogamp.java3d.Node;
import org.jogamp.java3d.PickInfo;
import org.jogamp.java3d.SceneGraphPath;

import com.eteks.sweethome3d.model.DimensionLine;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.Label;
import com.eteks.sweethome3d.model.Polyline;
import com.eteks.sweethome3d.model.Room;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.Wall;
import com.eteks.sweethome3d.viewcontroller.HomeController3D;
import com.jogamp.newt.event.MouseEvent;

public class HomeComponent3DMouseHandler extends MouseOverHandler {
	private final Home home;
	private final UserPreferences preferences;
	private final HomeController3D controller;

	public HomeComponent3DMouseHandler(Home home, UserPreferences preferences, HomeController3D controller) {
		this.home = home;
		this.preferences = preferences;
		this.controller = controller;
	}

	@Override
	public void doMouseClicked(MouseEvent e) {
		if(e.getPointerCount() == 1) {
			pickCanvas.setFlags(PickInfo.NODE | PickInfo.SCENEGRAPHPATH);
	
			pickCanvas.setShapeLocation(e.getX(), e.getY());
			PickInfo pickInfo = pickCanvas.pickClosest();
			if (pickInfo != null) {
				SceneGraphPath sg = pickInfo.getSceneGraphPath();
				Node pickedParent = sg.getNode(0);
				Object userData = pickedParent.getUserData();
	
				if (userData instanceof Selectable) {
					Selectable clickedSelectable = (Selectable) userData;
					ArrayList<Selectable> items = new ArrayList<Selectable>();
					items.add(clickedSelectable);
System.out.println("hit a selectable with " +e.getX()+ ", "+ e.getY());//DEBUG	
					this.home.setSelectedItems(items);
					this.home.setAllLevelsSelection(true);
					if (e.getClickCount() == 2) {
						// Modify selected item on a double click
						if (clickedSelectable instanceof Wall) {
							controller.modifySelectedWalls();						
						} else if (clickedSelectable instanceof HomePieceOfFurniture) {
							controller.modifySelectedFurniture();
						} else if (clickedSelectable instanceof Room) {
							controller.modifySelectedRooms();
						} else if (clickedSelectable instanceof Label) {
							controller.modifySelectedLabels();
						} else if (clickedSelectable instanceof DimensionLine) {
							controller.modifySelectedDimensionLines();
						} else if (clickedSelectable instanceof Polyline) {
							controller.modifySelectedPolylines();
						}
					}						
				} else {
					System.out.println("SceneGraphPath userData not a Selectable " + userData);//DEBUG
				}
			}
			else//DEBUG 
				System.out.println("Nope null PickInfo still! I've got the upper part to add o,but not the right side, as 0,0 is upper left of window");//DEBUG
		}
	}

	@Override
	public void doMousePressed(MouseEvent e) {

	}

	@Override
	public void doMouseReleased(MouseEvent mouseEvent) {

	}

}