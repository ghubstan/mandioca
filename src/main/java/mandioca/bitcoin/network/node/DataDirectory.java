package mandioca.bitcoin.network.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.Set;

import static java.nio.file.attribute.PosixFilePermission.*;
import static java.nio.file.attribute.PosixFilePermissions.asFileAttribute;
import static java.util.Arrays.stream;
import static mandioca.bitcoin.network.NetworkProperties.DATA_DIR;
import static mandioca.bitcoin.network.NetworkProperties.NETWORK;

final class DataDirectory {

    private static final Logger log = LoggerFactory.getLogger(DataDirectory.class);

    private static final String NETWORK_SPEC = NETWORK.name().toLowerCase();

    private static final Set<PosixFilePermission> PERMISSIONS =
            EnumSet.of(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE, GROUP_READ, GROUP_WRITE, OTHERS_READ);

    private static final FileAttribute<Set<PosixFilePermission>> FILE_ATTRIBUTES = asFileAttribute(PERMISSIONS);

    static void initDataDir() {
        String dataDirSpec = "file://" + DATA_DIR;
        try {
            Path dataDirPath = Paths.get(URI.create(dataDirSpec));
            log.debug("checking for presence of data.dir {}", dataDirPath.toAbsolutePath());
            if (!dataDirPath.isAbsolute()) {
                throw new RuntimeException(dataDirPath.toString() + " must be an absolute path");
            }
            if (!dataDirPath.toFile().exists()) {
                log.debug("creating data.dir {}", dataDirPath.toAbsolutePath());
                Files.createDirectories(dataDirPath, FILE_ATTRIBUTES);
            }
            Path blocksPath = initChildDir("blocks");
            Path chainstatePath = initChildDir("chainstate");
            Path indexesPath = initChildDir("indexes");
            Path walletsPath = initChildDir("wallets");
            validate(dataDirPath, blocksPath, chainstatePath, indexesPath, walletsPath);
            log.debug("data.dir {} exists", dataDirPath.toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("error setting up node's data dir tree" + dataDirSpec);
        }
    }

    private static Path initChildDir(String dirName) throws IOException {
        Path path = Paths.get(DATA_DIR, NETWORK_SPEC, dirName);
        if (!path.toFile().exists()) {
            log.debug("creating data.dir child {}", path.toAbsolutePath());
            return Files.createDirectories(path, FILE_ATTRIBUTES);
        } else {
            return path;
        }
    }

    private static void validate(Path... paths) {
        stream(paths).forEach(p -> {
            if (!p.toFile().exists()) {
                throw new RuntimeException("data.dir " + p.toString() + " not found or created during initialization");
            }
        });
    }
}
