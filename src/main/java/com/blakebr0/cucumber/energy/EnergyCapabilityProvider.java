package com.blakebr0.cucumber.energy;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

public class EnergyCapabilityProvider implements ICapabilityProvider {
	protected IEnergyStorage instance;
	
	public EnergyCapabilityProvider(IEnergyStorage instance) {
		this.instance = instance;
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction direction) {
		return ForgeCapabilities.ENERGY.orEmpty(capability, LazyOptional.of(() -> this.instance));
	}
}
