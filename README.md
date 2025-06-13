# ij2-StageMeasurementsBead
This is an ImageJ2 plugin to report on the position of a (fluorescent) bead in 3D. Goal is to report on the stability performance of a light microscope.

# Citing
Please cite this plugin by linking it to this GitHub or to the release you used, and feel free to give it a star ⭐️

## Code authorship
**Author**: Arne Seitz (1)

**Contributors**: QUAREP-LiMi workgroup 6

**Affiliations**
(1) EPFL BioImaging and Optics Platform (BIOP)

## Installation
Copy the following two jar files to the plugins folder of Fiji/ImageJ:
- StageMeasurementsBead-0.1.0-SNAPSHOT.jar
- StageMeasurementUtil-0.1.0-SNAPSHOT.jar
- The jars can be founf here:https://github.com/ArneS70/ij2-StageMeasurementsBead/releases/tag/v0.1-beta.1

## Workflow
- Open an image-stack of a fluorescent bead moving in 3D over time.
- Virtual images are supported by the plugin. This option is recommended for large image datasets.
- Run the plugin called "Localize Bead 3D". It can be found via the search functionality or in the submenue: Plugings-> StabilityMeasurement
