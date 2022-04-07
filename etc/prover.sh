# Assumes build is done up to 'mvn package'
# and there are no outdated jars in the target directory
~/jdk-17/bin/java -cp /mnt/c/olivine/target/olivine-*.jar olivine/Prover $@
