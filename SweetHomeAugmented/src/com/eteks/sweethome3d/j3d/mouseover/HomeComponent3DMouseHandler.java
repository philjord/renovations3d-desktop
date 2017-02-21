package com.eteks.sweethome3d.j3d.mouseover;

import java.util.ArrayList;

import org.jogamp.java3d.Node;
import org.jogamp.java3d.PickInfo;
import org.jogamp.java3d.SceneGraphPath;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.Room;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.Wall;
import com.eteks.sweethome3d.viewcontroller.HomeController3D;
import com.jogamp.newt.event.MouseEvent;

public class HomeComponent3DMouseHandler extends MouseOverHandler
{
	private final Home home;
	private final UserPreferences preferences;
	private final HomeController3D controller;

	public HomeComponent3DMouseHandler(Home home, UserPreferences preferences, HomeController3D controller)
	{
		this.home = home;
		this.preferences = preferences;
		this.controller = controller;
	}

	@Override
	public void doMouseClicked(MouseEvent e)
	{
		pickCanvas.setFlags(PickInfo.NODE | PickInfo.SCENEGRAPHPATH);

		pickCanvas.setShapeLocation(e.getX(), e.getY());
		PickInfo pickInfo = pickCanvas.pickClosest();
		if (pickInfo != null)
		{
			SceneGraphPath sg = pickInfo.getSceneGraphPath();
			Node pickedParent = sg.getNode(sg.nodeCount() - 1);
			Object userData = pickedParent.getUserData();

			if (userData instanceof Selectable)
			{
				Selectable clickedSelectable = (Selectable) userData;
				ArrayList<Selectable> items = new ArrayList<Selectable>();
				items.add(clickedSelectable);

				this.home.setSelectedItems(items);
				this.home.setAllLevelsSelection(true);
				if (e.getClickCount() == 2)
				{
					// Modify selected item on a double click
					if (clickedSelectable instanceof Wall)
					{
						controller.modifySelectedWalls();						
					}
					else if (clickedSelectable instanceof HomePieceOfFurniture)
					{
						controller.modifySelectedFurniture();
					}
					else if (clickedSelectable instanceof Room)
					{
						controller.modifySelectedRooms();
					}
					
					//FIXME: Label3D need to be selectable too but not Ground3D
				}
			}

		}
	}

	@Override
	public void doMousePressed(MouseEvent e)
	{

	}

	@Override
	public void doMouseReleased(MouseEvent mouseEvent)
	{

	}

}