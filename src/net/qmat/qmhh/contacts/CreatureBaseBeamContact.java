package net.qmat.qmhh.contacts;

import net.qmat.qmhh.models.Hand.Beam;
import net.qmat.qmhh.models.creatures.CreatureBase;
import org.jbox2d.dynamics.contacts.Contact;

public class CreatureBaseBeamContact extends ContactLogic {
	
	public void beginContact(Contact contact, Object objA, Object objB) {
		CreatureBase creature = (CreatureBase)objA;
		/* uncomment for blocking behaviour
		if(creature.stage > 0) {
			// TODO: this will cause trouble when the hand is suddenly removed asynchronously!
			Beam beam = (Beam)objB;
			beam.hand.addCreature(creature);
		}
		*/
	}
	
	public void endContact(Contact contact, Object objA, Object objB) {
		CreatureBase creature = (CreatureBase)objA;
		/* uncomment for blocking behaviour
		if(creature.stage > 0) {
			// TODO: this will cause trouble when the hand is suddenly removed asynchronously!
			Beam beam = (Beam)objB;
			beam.hand.removeCreature(creature);
		}
		*/
	}
}