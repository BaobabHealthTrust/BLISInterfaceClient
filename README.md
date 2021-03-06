This is my fork of the BLISInterfaceClient by ilabafrica.  I have modified this software in the following ways:
* a command-line only user interface (ui.Cli) so that it can run without a monitor - e.g., raspberry pi
* refactoring of the code to separate ui and business logic and employ OO architecture
* a new interface for the Mindray BC3000

# BLISInterfaceClient
BLIS Interface Client is a very lightweight Java based tool for Interfacing BLIS with equipment using Protocols like ;RS232,TCP/IP, HTTP. It can also connect directly to analyzer database such as MS Access or even from a flat file.

The Messaging formats supported are HL7, ASTM, ABX Proprietary Format, Vital Proprietary format, CLSI LIS2-A, CLSI LIS2-A2, etc


Requires Java Runtime 1.7 or above

Equipment supported by v2.6.12.1 below:
* ABX Pentra 60C+
* ABX MICROS 60
* ABX Pentra 80
* BD FACSCalibur
* BT3000 Plus*
* BT3000 Plus-Chameleon
* Flexor E
* Flexor Junior
* Selectra Junior
* Mindray BS-200E
* MINDRAY BC 3600
* MINDRAY BC 3000 (added by espinoj)
* SYSMEX XS-500i*
* SYSMEX XT-2000i
* GeneXpert

