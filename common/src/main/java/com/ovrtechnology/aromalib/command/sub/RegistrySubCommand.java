package com.ovrtechnology.aromalib.command.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.ovrtechnology.aromalib.registry.BiomeRegistry;
import com.ovrtechnology.aromalib.registry.BlockRegistry;
import com.ovrtechnology.aromalib.command.SubCommand;
import com.ovrtechnology.aromalib.registry.ScentRegistry;
import com.ovrtechnology.aromalib.registry.StructureRegistry;
import lombok.Getter;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Subcommand for listing registered items from various registries.
 * <p>
 * Usage:
 * <ul>
 *   <li>{@code /aromatest registry structure} - List all registered structures</li>
 *   <li>{@code /aromatest registry block} - List all registered blocks</li>
 *   <li>{@code /aromatest registry biome} - List all registered biomes</li>
 *   <li>{@code /aromatest registry scent} - List all registered scents</li>
 * </ul>
 */
public class RegistrySubCommand implements SubCommand {
    
    @Override
    public String getName() {
        return "registry";
    }
    
    @Override
    public String getDescription() {
        return "List registered structures, blocks, biomes, or scents";
    }
    
    @Override
    public ArgumentBuilder<CommandSourceStack, ?> build(LiteralArgumentBuilder<CommandSourceStack> builder) {
        return builder
                .then(Commands.literal("structure")
                        .executes(ctx -> executeList(ctx, RegistryType.STRUCTURE)))
                .then(Commands.literal("block")
                        .executes(ctx -> executeList(ctx, RegistryType.BLOCK)))
                .then(Commands.literal("biome")
                        .executes(ctx -> executeList(ctx, RegistryType.BIOME)))
                .then(Commands.literal("scent")
                        .executes(ctx -> executeList(ctx, RegistryType.SCENT)))
                .executes(this::showUsage);
    }
    
    private int showUsage(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        source.sendSuccess(() -> Component.literal("§6[Aroma Lib] §7Registry usage:"), false);
        source.sendSuccess(() -> Component.literal("§e  /aromatest registry structure §8- List structures"), false);
        source.sendSuccess(() -> Component.literal("§e  /aromatest registry block §8- List blocks"), false);
        source.sendSuccess(() -> Component.literal("§e  /aromatest registry biome §8- List biomes"), false);
        source.sendSuccess(() -> Component.literal("§e  /aromatest registry scent §8- List scents"), false);
        return Command.SINGLE_SUCCESS;
    }
    
    private int executeList(CommandContext<CommandSourceStack> context, RegistryType type) {
        CommandSourceStack source = context.getSource();
        
        List<String> ids = getRegisteredIds(type);
        int count = ids.size();
        
        if (count == 0) {
            source.sendSuccess(() -> Component.literal(
                    "§6[Aroma Lib] §7No " + type.getDisplayName() + " registered."
            ), false);
            return Command.SINGLE_SUCCESS;
        }
        
        // Header
        source.sendSuccess(() -> Component.literal(
                "§6[Aroma Lib] §7Registered " + type.getDisplayName() + " §8(§e" + count + "§8):"
        ), false);
        
        // List IDs comma-separated
        String idList = String.join(", ", ids);
        source.sendSuccess(() -> Component.literal("§f" + idList), false);
        
        return Command.SINGLE_SUCCESS;
    }
    
    private List<String> getRegisteredIds(RegistryType type) {
        List<String> ids = new ArrayList<>();
        
        switch (type) {
            case STRUCTURE -> {
                for (String id : StructureRegistry.INSTANCE.getIds()) {
                    ids.add(id);
                }
            }
            case BLOCK -> {
                for (String id : BlockRegistry.INSTANCE.getIds()) {
                    ids.add(id);
                }
            }
            case BIOME -> {
                for (String id : BiomeRegistry.INSTANCE.getIds()) {
                    ids.add(id);
                }
            }
            case SCENT -> {
                for (String id : ScentRegistry.INSTANCE.getIds()) {
                    ids.add(id);
                }
            }
        }
        
        return ids;
    }
    
    /**
     * Enum for registry types.
     */
    @Getter
    private enum RegistryType {
        STRUCTURE("structures"),
        BLOCK("blocks"),
        BIOME("biomes"),
        SCENT("scents");
        
        private final String displayName;
        
        RegistryType(String displayName) {
            this.displayName = displayName;
        }
    }
}

