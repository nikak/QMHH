package net.qmat.qmhh.contacts;

import net.qmat.qmhh.models.Background;
import net.qmat.qmhh.models.Models;
import net.qmat.qmhh.models.Spore;

import org.jbox2d.collision.Manifold;
import org.jbox2d.dynamics.contacts.Contact;

public class BackgroundSporeContact extends ContactLogic {
	
	public void contact(Contact contact, Manifold manifold, Object objA, Object objB) {
		//Background background = (Background)objA;
		contact.setEnabled(false);
		//Spore spore = (Spore)objB;
		//Models.getSporesModel().removeSpore(spore);
	}
}