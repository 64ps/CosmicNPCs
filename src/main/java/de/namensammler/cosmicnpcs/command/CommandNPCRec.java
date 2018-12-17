package de.namensammler.cosmicnpcs.command;

import java.util.List;

import de.namensammler.cosmicnpcs.CosmicNPCs;
import de.namensammler.cosmicnpcs.npcsystem.NPCRecorder;
import de.namensammler.cosmicnpcs.npcsystem.RecordThread;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;

public class CommandNPCRec extends CommandBase {
	CosmicNPCs parent;

	public CommandNPCRec(CosmicNPCs _parent) {
		parent = _parent;
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender icommandsender) {
		if (!(icommandsender instanceof EntityPlayer)) return false;

		EntityPlayer ep = (EntityPlayer) icommandsender;

		/* 1.7.10 "IsOpped" */
		return MinecraftServer
				.getServer()
				.getConfigurationManager()
				.func_152596_g(
						(ep.getGameProfile()));
	}

	@Override
	public String getCommandName() {
		return "npc-rec";
	}

	@Override
	public String getCommandUsage(ICommandSender icommandsender) {
		return "Usage: /npc-rec <savefile>, eg: /npc-rec leyla1";
	}

	@Override
	public void processCommand(ICommandSender icommandsender, String[] args) {
		EntityPlayer player;

		if (!(icommandsender instanceof EntityPlayer)) {
			return;
		}

		if (args.length < 1) {
			icommandsender.addChatMessage(new ChatComponentText(
					getCommandUsage(icommandsender)));
			return;
		}

		player = (EntityPlayer) icommandsender;

		/* Are we being recorded? */
		NPCRecorder aRecorder = CosmicNPCs.instance.recordThreads.get(player);

		if (aRecorder != null) {
			aRecorder.recordThread.capture = false;
			CosmicNPCs.instance.broadcastMsg("Stopped recording "
					+ player.getDisplayName() + " to file "
					+ aRecorder.fileName + ".action");
			CosmicNPCs.instance.recordThreads.remove(player);
			return;
		}

		/* Is this filename being recorded to? */

		synchronized (CosmicNPCs.instance.recordThreads) {
			for (NPCRecorder ar : CosmicNPCs.instance.recordThreads.values()) {
				if (ar.fileName.equals(args[0].toLowerCase())) {
					CosmicNPCs.instance.broadcastMsg("'" + ar.fileName
							+ ".action' is already being recorded to?");
					return;
				}
			}
		}

		if (aRecorder == null) {
			CosmicNPCs.instance.broadcastMsg("Started recording "
					+ player.getDisplayName() + " to file " + args[0]
							+ ".action");
			NPCRecorder mcr = new NPCRecorder();
			mcr.fileName = args[0].toLowerCase();
			CosmicNPCs.instance.recordThreads.put(player, mcr);
			mcr.recordThread = new RecordThread(player, args[0]);
			return;
		}

	}
}
