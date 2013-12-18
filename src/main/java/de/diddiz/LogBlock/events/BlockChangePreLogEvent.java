package de.diddiz.LogBlock.events;
import de.diddiz.LogBlock.ChestAccess;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.event.HandlerList;

public class BlockChangePreLogEvent extends PreLogEvent {

	private static final HandlerList handlers = new HandlerList();
	private Location location;
	private int typeBefore, typeAfter;
	private byte data;
	private String signText;
	private ChestAccess chestAccess;

	public BlockChangePreLogEvent(String owner, Location location, int typeBefore, int typeAfter, byte data,
								  String signText, ChestAccess chestAccess) {

		super(owner);
		this.location = location;
		this.typeBefore = typeBefore;
		this.typeAfter = typeAfter;
		this.data = data;
		this.signText = signText;
		this.chestAccess = chestAccess;
	}

	public Location getLocation() {

		return this.location;
	}

	public void setLocation(Location location) {

		this.location = location;
	}

	public int getTypeBefore() {

		return this.typeBefore;
	}

	public void setTypeBefore(int typeBefore) {

		this.typeBefore = typeBefore;
	}

	public int getTypeAfter() {

		return this.typeAfter;
	}

	public void setTypeAfter(int typeAfter) {

		this.typeAfter = typeAfter;
	}

	public byte getData() {

		return this.data;
	}

	public void setData(byte data) {

		this.data = data;
	}

	public String getSignText() {

		return this.signText;
	}

	public void setSignText(String[] signText) {

		if (signText != null) {
			// Check for block
			Validate.isTrue(this.isValidSign(), "Must be valid sign block");

			// Check for problems
			Validate.noNullElements(signText, "No null lines");
			Validate.isTrue(signText.length == 4, "Sign text must be 4 strings");

			this.signText = signText[0] + "\0" + signText[1] + "\0" + signText[2] + "\0" + signText[3];
		} else {
			this.signText = null;
		}
	}

	private boolean isValidSign() {

		if (((this.typeAfter == 63) || (this.typeAfter == 68)) && (this.typeBefore == 0)) {
			return true;
		}
		if (((this.typeBefore == 63) || (this.typeBefore == 68)) && (this.typeAfter == 0)) {
			return true;
		}
		if (((this.typeAfter == 63) || (this.typeAfter == 68)) && (this.typeBefore == this.typeAfter)) {
			return true;
		}
		return false;
	}

	public ChestAccess getChestAccess() {

		return this.chestAccess;
	}

	public void setChestAccess(ChestAccess chestAccess) {

		this.chestAccess = chestAccess;
	}

	@Override
	public HandlerList getHandlers() {

		return BlockChangePreLogEvent.handlers;
	}

	public static HandlerList getHandlerList() {

		return BlockChangePreLogEvent.handlers;
	}
}
