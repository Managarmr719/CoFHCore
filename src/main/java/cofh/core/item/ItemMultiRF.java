package cofh.core.item;

import cofh.api.item.IMultiModeItem;
import cofh.core.init.CoreEnchantments;
import cofh.core.init.CoreProps;
import cofh.core.util.helpers.EnergyHelper;
import cofh.core.util.helpers.ItemHelper;
import cofh.redstoneflux.api.IEnergyContainerItem;
import cofh.redstoneflux.util.EnergyContainerItemWrapper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public abstract class ItemMultiRF extends ItemMulti implements IMultiModeItem, IEnergyContainerItem, IEnchantableItem {

	public ItemMultiRF(String modName) {

		super(modName);
	}

	@Override
	public boolean isFull3D() {

		return true;
	}

	@Override
	public boolean isEnchantable(ItemStack stack) {

		return ItemHelper.getItemDamage(stack) != CREATIVE;
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {

		return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged) && (slotChanged || getEnergyStored(oldStack) > 0 != getEnergyStored(newStack) > 0);
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack) {

		return ItemHelper.getItemDamage(stack) != CREATIVE;
	}

	@Override
	public int getRGBDurabilityForDisplay(ItemStack stack) {

		return CoreProps.RGB_DURABILITY_FLUX;
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {

		if (stack.getTagCompound() == null) {
			EnergyHelper.setDefaultEnergyTag(stack, 0);
		}
		return 1.0D - ((double) stack.getTagCompound().getInteger("Energy") / (double) getMaxEnergyStored(stack));
	}

	/* HELPERS */
	protected abstract int getCapacity(ItemStack stack);

	protected abstract int getReceive(ItemStack stack);

	/* IEnergyContainerItem */
	@Override
	public int receiveEnergy(ItemStack container, int maxReceive, boolean simulate) {

		if (container.getTagCompound() == null) {
			EnergyHelper.setDefaultEnergyTag(container, 0);
		}
		int stored = container.getTagCompound().getInteger("Energy");
		int receive = Math.min(maxReceive, Math.min(getMaxEnergyStored(container) - stored, getReceive(container)));

		if (!simulate && ItemHelper.getItemDamage(container) != CREATIVE) {
			stored += receive;
			container.getTagCompound().setInteger("Energy", stored);
		}
		return receive;
	}

	@Override
	public int extractEnergy(ItemStack container, int maxExtract, boolean simulate) {

		if (container.getTagCompound() == null) {
			EnergyHelper.setDefaultEnergyTag(container, 0);
		}
		if (ItemHelper.getItemDamage(container) == CREATIVE) {
			return maxExtract;
		}
		int stored = container.getTagCompound().getInteger("Energy");
		int extract = Math.min(maxExtract, stored);

		if (!simulate) {
			stored -= extract;
			container.getTagCompound().setInteger("Energy", stored);
		}
		return extract;
	}

	@Override
	public int getEnergyStored(ItemStack container) {

		if (container.getTagCompound() == null) {
			EnergyHelper.setDefaultEnergyTag(container, 0);
		}
		return container.getTagCompound().getInteger("Energy");
	}

	@Override
	public int getMaxEnergyStored(ItemStack container) {

		return getCapacity(container);
	}

	/* IEnchantableItem */
	@Override
	public boolean canEnchant(ItemStack stack, Enchantment enchantment) {

		return enchantment == CoreEnchantments.holding;
	}

	/* CAPABILITIES */
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {

		return new EnergyContainerItemWrapper(stack, this);
	}

	public static final int CREATIVE = 32000;

}
