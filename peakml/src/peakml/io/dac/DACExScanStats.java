/* Copyright (C) 2008, Groningen Bioinformatics Centre (http://gbic.biol.rug.nl/)
 * This file is part of PeakML.
 * 
 * PeakML is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * PeakML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with PeakML; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */



package peakml.io.dac;


// java





/**
 * 
 */
public class DACExScanStats
{
	// DACExScanStats mapping
	public native int open(String filename, int functionnr, int processnr, int scannr);
	
	
	// access
	public boolean getAccurateMass()
	{
		return AccurateMass;
	}
	
	public int getAccurateMassFlags()
	{
		return AccurateMassFlags;
	}
	
	public int getAccVoltage()
	{
		return AccVoltage;
	}
	
	public int getCoarseLaserControl()
	{
		return CoarseLaserControl;
	}
	
	public double getCollisionEnergy()
	{
		return CollisionEnergy;
	}
	
	public int getCollisionRF()
	{
		return CollisionRF;
	}
	
	public int getCounterElectrodeVoltage()
	{
		return CounterElectrodeVoltage;
	}
	
	public int getEntrance()
	{
		return Entrance;
	}
	
	public double getFaimsCV()
	{
		return FaimsCV;
	}
	
	public int getFineLaserControl()
	{
		return FineLaserControl;
	}
	
	public int getFocus()
	{
		return Focus;
	}
	
	public int getGuard()
	{
		return Guard;
	}
	
	public int getHMResolution()
	{
		return HMResolution;
	}
	
	public int getIonEnergy()
	{
		return IonEnergy;
	}
	
	public double getLaserAimXPos()
	{
		return LaserAimXPos;
	}
	
	public double getLaserAimYPos()
	{
		return LaserAimYPos;
	}
	
	public int getLaserRepetitionRate()
	{
		return LaserRepetitionRate;
	}
	
	public int getLinearDetectorVoltage()
	{
		return LinearDetectorVoltage;
	}
	
	public int getLinearSensitivity()
	{
		return LinearSensitivity;
	}
	
	public int getLMResolution()
	{
		return LMResolution;
	}
	
	public double getLockMassCorrection()
	{
		return LockMassCorrection;
	}
	
	public int getMultiplier1()
	{
		return Multiplier1;
	}
	
	public int getMultiplier2()
	{
		return Multiplier2;
	}
	
	public int getNeedle()
	{
		return Needle;
	}
	
	public int getNumShotsPerformed()
	{
		return NumShotsPerformed;
	}
	
	public int getNumShotsSummed()
	{
		return NumShotsSummed;
	}
	
	public int getProbeTemperature()
	{
		return ProbeTemperature;
	}
	
	public double getPSDFactor1()
	{
		return PSDFactor1;
	}
	
	public double getPSDMajorStep()
	{
		return PSDMajorStep;
	}
	
	public double getPSDMinorStep()
	{
		return PSDMinorStep;
	}
	
	public int getPSDSegmentType()
	{
		return PSDSegmentType;
	}
	
	public byte getReferenceScan()
	{
		return ReferenceScan;
	}
	
	public int getReflectron()
	{
		return Reflectron;
	}
	
	public int getReflectronDetectorVoltage()
	{
		return ReflectronDetectorVoltage;
	}
	
	public double getReflectronFieldLength()
	{
		return ReflectronFieldLength;
	}
	
	public double getReflectronFieldLengthAlt()
	{
		return ReflectronFieldLengthAlt;
	}
	
	public double getReflectronLength()
	{
		return ReflectronLength;
	}
	
	public double getReflectronLengthAlt()
	{
		return ReflectronLengthAlt;
	}
	
	public int getReflectronLensVoltage()
	{
		return ReflectronLensVoltage;
	}
	
	public int getReflectronSensitivity()
	{
		return ReflectronSensitivity;
	}
	
	public double getReflectronVoltage()
	{
		return ReflectronVoltage;
	}
	
	public int getRFVoltage()
	{
		return RFVoltage;
	}
	
	public double getSamplePlateVoltage()
	{
		return SamplePlateVoltage;
	}
	
	public int getSamplingConeVoltage()
	{
		return SamplingConeVoltage;
	}
	
	public int getSegmentNumber()
	{
		return SegmentNumber;
	}
	
	public double getSetMass()
	{
		return SetMass;
	}
	
	public int getSkimmer()
	{
		return Skimmer;
	}
	
	public int getSkimmerLens()
	{
		return SkimmerLens;
	}
	
	public int getSourceAperture()
	{
		return SourceAperture;
	}
	
	public int getSourceCode()
	{
		return SourceCode;
	}
	
	public double getSourceRegion1()
	{
		return SourceRegion1;
	}
	
	public double getSourceRegion2()
	{
		return SourceRegion2;
	}
	
	public int getSourceTemperature()
	{
		return SourceTemperature;
	}
	
	public int getSteering()
	{
		return Steering;
	}
	
	public double getTempCoefficient()
	{
		return TempCoefficient;
	}
	
	public double getTempCorrection()
	{
		return TempCorrection;
	}
	
	public int getTFMWell()
	{
		return TFMWell;
	}
	
	public double getTIC_A()
	{
		return TIC_A;
	}
	
	public double getTIC_B()
	{
		return TIC_B;
	}
	
	public int getTOF()
	{
		return TOF;
	}
	
	public int getTOFAperture()
	{
		return TOFAperture;
	}
	
	public int getTransportDC()
	{
		return TransportDC;
	}
	
	public int getTransportRF()
	{
		return TransportRF;
	}
	
	public byte getUseLockMassCorrection()
	{
		return UseLockMassCorrection;
	}
	
	public byte getUseTempCorrection()
	{
		return UseTempCorrection;
	}
	
	
	// Object overrides
	public String toString()
	{
		StringBuffer str = new StringBuffer();
		
		str.append("DACExScanStats {\n");
		str.append("  AccurateMass:              " + AccurateMass + "\n");
		str.append("  AccurateMassFlags:         " + AccurateMassFlags + "\n");
		str.append("  AccVoltage:                " + AccVoltage + "\n");
		str.append("  CoarseLaserControl:        " + CoarseLaserControl + "\n");
		str.append("  CollisionEnergy:           " + CollisionEnergy + "\n");
		str.append("  CollisionRF:               " + CollisionRF + "\n");
		str.append("  CounterElectrodeVoltage:   " + CounterElectrodeVoltage + "\n");
		str.append("  Entrance:                  " + Entrance + "\n");
		str.append("  FaimsCV:                   " + FaimsCV + "\n");
		str.append("  FineLaserControl:          " + FineLaserControl + "\n");
		str.append("  Focus:                     " + Focus + "\n");
		str.append("  Guard:                     " + Guard + "\n");
		str.append("  HMResolution:              " + HMResolution + "\n");
		str.append("  IonEnergy:                 " + IonEnergy + "\n");
		str.append("  LaserAimXPos:              " + LaserAimXPos + "\n");
		str.append("  LaserAimYPos:              " + LaserAimYPos + "\n");
		str.append("  LaserRepetitionRate:       " + LaserRepetitionRate + "\n");
		str.append("  LinearDetectorVoltage:     " + LinearDetectorVoltage + "\n");
		str.append("  LinearSensitivity:         " + LinearSensitivity + "\n");
		str.append("  LMResolution:              " + LMResolution + "\n");
		str.append("  LockMassCorrection:        " + LockMassCorrection + "\n");
		str.append("  Multiplier1:               " + Multiplier1 + "\n");
		str.append("  Multiplier2:               " + Multiplier2 + "\n");
		str.append("  Needle:                    " + Needle + "\n");
		str.append("  NumShotsPerformed:         " + NumShotsPerformed + "\n");
		str.append("  NumShotsSummed:            " + NumShotsSummed + "\n");
		str.append("  ProbeTemperature:          " + ProbeTemperature + "\n");
		str.append("  PSDFactor1:                " + PSDFactor1 + "\n");
		str.append("  PSDMajorStep:              " + PSDMajorStep + "\n");
		str.append("  PSDMinorStep:              " + PSDMinorStep + "\n");
		str.append("  PSDSegmentType:            " + PSDSegmentType + "\n");
		str.append("  ReferenceScan:             " + ReferenceScan + "\n");
		str.append("  Reflectron:                " + Reflectron + "\n");
		str.append("  ReflectronDetectorVoltage: " + ReflectronDetectorVoltage + "\n");
		str.append("  ReflectronFieldLength:     " + ReflectronFieldLength + "\n");
		str.append("  ReflectronFieldLengthAlt:  " + ReflectronFieldLengthAlt + "\n");
		str.append("  ReflectronLength:          " + ReflectronLength + "\n");
		str.append("  ReflectronLengthAlt:       " + ReflectronLengthAlt + "\n");
		str.append("  ReflectronLensVoltage:     " + ReflectronLensVoltage + "\n");
		str.append("  ReflectronSensitivity:     " + ReflectronSensitivity + "\n");
		str.append("  ReflectronVoltage:         " + ReflectronVoltage + "\n");
		str.append("  RFVoltage:                 " + RFVoltage + "\n");
		str.append("  SamplePlateVoltage:        " + SamplePlateVoltage + "\n");
		str.append("  SamplingConeVoltage:       " + SamplingConeVoltage + "\n");
		str.append("  SegmentNumber:             " + SegmentNumber + "\n");
		str.append("  SetMass:                   " + SetMass + "\n");
		str.append("  Skimmer:                   " + Skimmer + "\n");
		str.append("  SkimmerLens:               " + SkimmerLens + "\n");
		str.append("  SourceAperture:            " + SourceAperture + "\n");
		str.append("  SourceCode:                " + SourceCode + "\n");
		str.append("  SourceRegion1:             " + SourceRegion1 + "\n");
		str.append("  SourceRegion2:             " + SourceRegion2 + "\n");
		str.append("  SourceTemperature:         " + SourceTemperature + "\n");
		str.append("  Steering:                  " + Steering + "\n");
		str.append("  TempCoefficient:           " + TempCoefficient + "\n");
		str.append("  TempCorrection:            " + TempCorrection + "\n");
		str.append("  TFMWell:                   " + TFMWell + "\n");
		str.append("  TIC_A:                     " + TIC_A + "\n");
		str.append("  TIC_B:                     " + TIC_B + "\n");
		str.append("  TOF:                       " + TOF + "\n");
		str.append("  TOFAperture:               " + TOFAperture + "\n");
		str.append("  TransportDC:               " + TransportDC + "\n");
		str.append("  TransportRF:               " + TransportRF + "\n");
		str.append("  UseLockMassCorrection:     " + UseLockMassCorrection + "\n");
		str.append("  UseTempCorrection:         " + UseTempCorrection + "\n");
		str.append("}\n");
		
		return str.toString();
	}
	
	
	// data
	protected boolean AccurateMass;
	protected int AccurateMassFlags;
	protected int AccVoltage;
	protected int CoarseLaserControl;
	protected double CollisionEnergy;
	protected int CollisionRF;
	protected int CounterElectrodeVoltage;
	protected int Entrance;
	protected double FaimsCV;
	protected int FineLaserControl;
	protected int Focus;
	protected int Guard;
	protected int HMResolution;
	protected int IonEnergy;
	protected double LaserAimXPos;
	protected double LaserAimYPos;
	protected int LaserRepetitionRate;
	protected int LinearDetectorVoltage;
	protected int LinearSensitivity;
	protected int LMResolution;
	protected double LockMassCorrection;
	protected int Multiplier1;
	protected int Multiplier2;
	protected int Needle;
	protected int NumShotsPerformed;
	protected int NumShotsSummed;
	protected int ProbeTemperature;
	protected double PSDFactor1;
	protected double PSDMajorStep;
	protected double PSDMinorStep;
	protected int PSDSegmentType;
	protected byte ReferenceScan;
	protected int Reflectron;
	protected int ReflectronDetectorVoltage;
	protected double ReflectronFieldLength;
	protected double ReflectronFieldLengthAlt;
	protected double ReflectronLength;
	protected double ReflectronLengthAlt;
	protected int ReflectronLensVoltage;
	protected int ReflectronSensitivity;
	protected double ReflectronVoltage;
	protected int RFVoltage;
	protected double SamplePlateVoltage;
	protected int SamplingConeVoltage;
	protected int SegmentNumber;
	protected double SetMass;
	protected int Skimmer;
	protected int SkimmerLens;
	protected int SourceAperture;
	protected int SourceCode;
	protected double SourceRegion1;
	protected double SourceRegion2;
	protected int SourceTemperature;
	protected int Steering;
	protected double TempCoefficient;
	protected double TempCorrection;
	protected int TFMWell;
	protected double TIC_A;
	protected double TIC_B;
	protected int TOF;
	protected int TOFAperture;
	protected int TransportDC;
	protected int TransportRF;
	protected byte UseLockMassCorrection;
	protected byte UseTempCorrection;
}
