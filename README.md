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
- StageMeasurementsBead-0.1.0-SNAPSHOT.jar, StageMeasurementUtil-0.1.0-SNAPSHOT.jar
- The jars can be found here:
https://github.com/ArneS70/ij2-StageMeasurementsBead/releases/tag/v0.1-beta.1

## Workflow
- Open an image-stack of a fluorescent bead moving in 3D over time. The plugin is desigend for image stack with one bead. The tracking of multiple beads is not supported and might result in ambigious results.
- Virtual images are supported by the plugin. This option is recommended for large image datasets.
- Run the plugin called "Localize Bead 3D". It can be found via the search functionality or in the submenue: Plugings-> StabilityMeasurement
- The following window allows to select parameters and options:
<img src="https://github.com/user-attachments/assets/bd91153f-6fce-476a-aedd-a2c223508937" width="300" height="300" />

- Parameters
   - "Bead size in um" enter the size of the bead in $\mu$ m.
   - "Time gap" can be used to skip frames; 1=use all frames.
   - "Method" Localization method to detect the bead. 
       - "Simple" binary image is obtained via thresholding and the center of mass is calculated (pixel resolution).
       - "Ellipse" binary imae is obtained via thresholding; center of mass is obtained by Fiji/ImageJ ellipse fitting (sub pixel resolution).
       - "Super Gauss" Super Gauss function is fitted to a line profile through the bead in the x and y direction (sub pixel resolution).
       - "2D Gauss Fit" using the method from Dominic Waithe:https://biii.eu/2d-gaussian-fitting-macro-fijiimagej-multiple-signals (sub pixel resolution).
       - "2D Gauss Fit" same as above with sigma being identical for x and y direction (sub pixel resolution).
  - For standard applications "Ellipse" method is recommended.
- Options
  - "show Rois" region of interests are added to the RoiManager.
  - "Summarize results" Mean, min, MAx and STDEV will be calculated for all of the timepoints.
  - "show Fit Window(s)" shows the line fits for the methods Super Gauss and 2D Gauss fit.
  - "show Drift Plot" plots the drift along the 3 axis (x,y,z) over time. Additionally the euclidan distance is calculated.
- Press "ok" to run the plugin.
- With none of the options checked a results table is displayed.   
<img src="https://github.com/user-attachments/assets/27ad5f77-b414-4f93-9362-2438a5dbf460" width="550" height="350" />

- The plugin can be called from the Fiji/ImageJ maco language, e.g. run("Localize Bead 3D", "sizebead=4 gap=15 method=Ellipse showrois=false summarize=true showfit=false showdrift=false").
