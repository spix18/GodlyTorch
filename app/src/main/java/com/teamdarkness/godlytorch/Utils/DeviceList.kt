/*
 * This file is part of Godly Torch.
 *
 *     Godly Torch is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Godly Torch is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Godly Torch.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.teamdarkness.godlytorch.Utils

import java.util.ArrayList

object DeviceList {

    private var deviceList: ArrayList<Device> = java.util.ArrayList()

    fun getDevices(): ArrayList<Device> {
        deviceList.clear()
        // HTC
        deviceList.add(
                Device().setName("HTC 10")
                        .setDeviceId("pme")
                        .isDualTone(true)
                        .setYellowLedFileLocation("led:torch_1/brightness")
                        .setWhiteLedFileLocation("led:torch_0/brightness")
                        .setToggleFileLocation("led:switch/brightness")
        )
        // LeEco
        deviceList.add(
                Device().setName("LeEco Le 2")
                        .setDeviceId("le_s2")
                        .isDualTone(true)
                        .setYellowLedFileLocation("led:torch_0/brightness")
                        .setWhiteLedFileLocation("led:torch_1/brightness")
                        .setToggleFileLocation("led:switch/brightness")
        )
        deviceList.add(
                Device().setName("LeEco LePro 3")
                        .setDeviceId("le_zl1")
                        .isDualTone(true)
                        .setYellowLedFileLocation("led:torch_0/brightness")
                        .setWhiteLedFileLocation("led:torch_1/brightness")
                        .setToggleFileLocation("led:switch/brightness")
        )
        // Lenovo
        deviceList.add(
                Device().setName("Lenovo P2 (kuntao)")
                        .setDeviceId("kuntao")
                        .isDualTone(true)
                        .setYellowLedFileLocation("led:torch_0/brightness")
                        .setWhiteLedFileLocation("led:torch_1/brightness")
                        .setToggleFileLocation("led:switch/brightness")
        )
        deviceList.add(
                Device().setName("Lenovo P2 (p2a42)")
                        .setDeviceId("p2a42")
                        .isDualTone(true)
                        .setYellowLedFileLocation("led:torch_0/brightness")
                        .setWhiteLedFileLocation("led:torch_1/brightness")
                        .setToggleFileLocation("led:switch/brightness")
        )
        deviceList.add(
                Device().setName("Lenovo P2 (p2c72)")
                        .setDeviceId("p2c72")
                        .isDualTone(true)
                        .setYellowLedFileLocation("led:torch_0/brightness")
                        .setWhiteLedFileLocation("led:torch_1/brightness")
                        .setToggleFileLocation("led:switch/brightness")
        )
        deviceList.add(
                Device().setName("Lenovo P2 (p2)")
                        .setDeviceId("p2")
                        .isDualTone(true)
                        .setYellowLedFileLocation("led:torch_0/brightness")
                        .setWhiteLedFileLocation("led:torch_1/brightness")
                        .setToggleFileLocation("led:switch/brightness")
        )
        deviceList.add(
                Device().setName("Lenovo K53")
                        .setDeviceId("karatep")
                        .isDualTone(true)
                        .setYellowLedFileLocation("led:torch_0/brightness")
                        .setWhiteLedFileLocation("led:torch_1/brightness")
                        .setToggleFileLocation("led:switch/brightness")
        )
        deviceList.add(
                Device().setName("Lenovo Vibe P1 Turbo")
                        .setDeviceId("passion")
                        .isDualTone(true)
                        .setYellowLedFileLocation("lenovo_torch1/brightness")
                        .setWhiteLedFileLocation("lenovo_torch0/brightness")
                        .setToggleFileLocation("dummy") // dummy because it is not required
        )
        deviceList.add(
                Device().setName("Lenovo A6000")
                        .setDeviceId("a6000")
                        .isDualTone(false)
                        .setSingleLedFileLocation("flashlight/brightness")
        )
        // Xiaomi
        deviceList.add(
                Device().setName("Mi 5")
                        .setDeviceId("gemini")
                        .setAliases("gemini_global")
                        .isDualTone(true)
                        .setYellowLedFileLocation("led:torch_0/brightness")
                        .setWhiteLedFileLocation("led:torch_1/brightness")
                        .setToggleFileLocation("led:switch/brightness")
        )
        deviceList.add(
                Device().setName("Mi 6")
                        .setDeviceId("sagit")
                        .setAliases("wayne")
                        .isDualTone(true)
                        .setYellowLedFileLocation("led:torch_0/brightness")
                        .setWhiteLedFileLocation("led:torch_1/brightness")
                        .setToggleFileLocation("led:switch_0/brightness")
        )
        deviceList.add(
                Device().setName("Mi 5s Plus")
                        .setDeviceId("natrium")
                        .isDualTone(true)
                        .setYellowLedFileLocation("led:torch_0/brightness")
                        .setWhiteLedFileLocation("led:torch_1/brightness")
                        .setToggleFileLocation("led:switch/brightness")
        )
        deviceList.add(
                Device().setName("Mi 5s")
                        .setDeviceId("capricorn")
                        .isDualTone(true)
                        .setYellowLedFileLocation("led:torch_0/brightness")
                        .setWhiteLedFileLocation("led:torch_1/brightness")
                        .setToggleFileLocation("led:switch/brightness")
        )
        deviceList.add(
                Device().setName("Mi A1")
                        .setDeviceId("tissot_sprout")
                        .setAliases("tissot")
                        .isDualTone(true)
                        .setYellowLedFileLocation("led:torch_1/brightness")
                        .setWhiteLedFileLocation("led:torch_0/brightness")
                        .setToggleFileLocation("led:switch/brightness")
        )
        deviceList.add(
                Device().setName("Mi Max (hydrogen)")
                        .setDeviceId("hydrogen")
                        .isDualTone(true)
                        .setYellowLedFileLocation("led:torch_1/brightness")
                        .setWhiteLedFileLocation("led:torch_0/brightness")
                        .setToggleFileLocation("led:switch/brightness")
        )
        deviceList.add(
                Device().setName("Mi Note 3")
                        .setDeviceId("jason")
                        .isDualTone(true)
                        .setYellowLedFileLocation("led:torch_0/brightness")
                        .setWhiteLedFileLocation("led:torch_1/brightness")
                        .setToggleFileLocation("led:switch_0/brightness")
        )
        // Motorola
        deviceList.add(
                Device().setName("Moto G5 Plus")
                        .setDeviceId("potter")
                        .setAliases("potter_n")
                        .isDualTone(true)
                        .setYellowLedFileLocation("led:torch_1/brightness")
                        .setWhiteLedFileLocation("led:torch_0/brightness")
                        .setToggleFileLocation("led:switch/brightness")
        )
        deviceList.add(
                Device().setName("Moto G5")
                        .setDeviceId("cedric")
                        .setAliases("cedric_global")
                        .isDualTone(true)
                        .setYellowLedFileLocation("led:torch_0/brightness")
                        .setWhiteLedFileLocation("led:torch_1/brightness")
                        .setToggleFileLocation("led:switch/brightness")
        )
        deviceList.add(
                Device().setName("Moto G4")
                        .setDeviceId("athene")
                        .setAliases("athene_p", "harpia")
                        .isDualTone(true)
                        .setYellowLedFileLocation("led:torch_0/brightness")
                        .setWhiteLedFileLocation("led:torch_1/brightness")
                        .setToggleFileLocation("led:switch/brightness")
        )
        deviceList.add(
                Device().setName("Moto G4 Plus")
                        .setDeviceId("athene_f")
                        .setAliases("athene_f")
                        .isDualTone(true)
                        .setYellowLedFileLocation("led:torch_0/brightness")
                        .setWhiteLedFileLocation("led:torch_1/brightness")
                        .setToggleFileLocation("led:switch/brightness")
        )
        deviceList.add(
                Device().setName("Moto G5S Plus")
                        .setDeviceId("sanders")
                        .isDualTone(true)
                        .setYellowLedFileLocation("led:torch_1/brightness")
                        .setWhiteLedFileLocation("led:torch_0/brightness")
                        .setToggleFileLocation("led:switch/brightness")
        )
        //Nexus
        deviceList.add(
                Device().setName("Nexus 5X")
                        .setDeviceId("bullhead")
                        .isDualTone(true)
                        .setYellowLedFileLocation("led:torch_1/brightness")
                        .setWhiteLedFileLocation("led:torch_0/brightness")
                        .setToggleFileLocation("led:switch/brightness")
        )
        deviceList.add(
                Device().setName("Nexus 6P")
                        .setDeviceId("angler")
                        .isDualTone(true)
                        .setYellowLedFileLocation("led:torch_1/brightness")
                        .setWhiteLedFileLocation("led:torch_0/brightness")
                        .setToggleFileLocation("led:switch/brightness")
        )
        // Oneplus
        deviceList.add(
                Device().setName("OnePlus One")
                        .setDeviceId("bacon")
                        .isDualTone(false)
                        .setSingleLedFileLocation("torch-light/brightness")
        )
        deviceList.add(
                Device().setName("OnePlus 2")
                        .setDeviceId("OnePlus2")
                        .isDualTone(true)
                        .setYellowLedFileLocation("led:torch_0/brightness")
                        .setWhiteLedFileLocation("led:torch_1/brightness")
                        .setToggleFileLocation("dummy")
        )
        deviceList.add(
                Device().setName("OnePlus 3")
                        .setDeviceId("oneplus3")
                        .isDualTone(true)
                        .setYellowLedFileLocation("led:torch_0/brightness")
                        .setWhiteLedFileLocation("led:torch_1/brightness")
                        .setToggleFileLocation("led:switch/brightness")
        )
        deviceList.add(
                Device().setName("OnePlus 5")
                        .setDeviceId("OnePlus5")
                        .setAliases("A5000")
                        .isDualTone(true)
                        .setYellowLedFileLocation("led:torch_0/brightness")
                        .setWhiteLedFileLocation("led:torch_1/brightness")
                        .setToggleFileLocation("led:switch_0/brightness")
        )
        deviceList.add(
                Device().setName("OnePlus 5t")
                        .setDeviceId("OnePlus5t")
                        .setAliases("A5010")
                        .isDualTone(true)
                        .setYellowLedFileLocation("led:torch_0/brightness")
                        .setWhiteLedFileLocation("led:torch_1/brightness")
                        .setToggleFileLocation("led:switch_0/brightness")
        )
        deviceList.add(
                Device().setName("OnePlus 8T")
                        .setDeviceId("kebab")
                        .setAliases("KB2000", "KB2001", "KB2003", "KB2005")
                        .isDualTone(true)
                        .setWhiteLedFileLocation("led:torch_0/brightness")
                        .setYellowLedFileLocation("led:torch_1/brightness")
                        .setToggleFileLocation("led:switch_2/brightness")
        )
        deviceList.add(
                Device().setName("OnePlus 9")
                        .setDeviceId("lemonadep")
                        .setAliases("LE2110", "LE2113", "LE2117")
                        .isDualTone(true)
                        .setWhiteLedFileLocation("led:torch_0/brightness")
                        .setYellowLedFileLocation("led:torch_1/brightness")
                        .setToggleFileLocation("led:switch_2/brightness")
        )
        deviceList.add(
                Device().setName("OnePlus 9R")
                        .setDeviceId("lemonades")
                        .setAliases("OnePlus9R", "LE2100")
                        .isDualTone(true)
                        .setWhiteLedFileLocation("led:torch_0/brightness")
                        .setYellowLedFileLocation("led:torch_1/brightness")
                        .setToggleFileLocation("led:switch_2/brightness")
        )
        // PIXEL
        deviceList.add(
                Device().setName("Pixel 2 XL")
                        .setDeviceId("taimen")
                        .isDualTone(true)
                        .setYellowLedFileLocation("led:torch_0/brightness")
                        .setWhiteLedFileLocation("led:torch_1/brightness")
                        .setToggleFileLocation("led:switch_0/brightness")
        )
        //Redmi series
        deviceList.add(
                Device().setName("Redmi 3S")
                        .setDeviceId("land")
                        .setAliases("landtoni")
                        .isDualTone(false)
                        //.setYellowLedFileLocation("led:torch_0/brightness")
                        //.setWhiteLedFileLocation("led:torch_1/brightness")
                        .setToggleFileLocation("led:switch/brightness")
        )
        deviceList.add(
                Device().setName("Redmi 4A")
                        .setDeviceId("rolex")
                        .setAliases("riva")
                        .isDualTone(false)
                        .setSingleLedFileLocation("flashlight/brightness")
        )

        deviceList.add(
                Device().setName("Redmi Note 3")
                        .setDeviceId("kenzo")
                        .isDualTone(false)
                        .setSingleLedFileLocation("flashlight/brightness")
        )
        deviceList.add(
                Device().setName("Redmi Note 4")
                        .setDeviceId("mido")
                        .setAliases("nikel")
                        .isDualTone(true)
                        .setYellowLedFileLocation("led:torch_1/brightness")
                        .setWhiteLedFileLocation("led:torch_0/brightness")
                        .setToggleFileLocation("led:switch/brightness")
        )
        deviceList.add(
                Device().setName("Redmi Note 5 Pro")
                        .setDeviceId("whyred")
                        .isDualTone(true)
                        .setYellowLedFileLocation("led:torch_1/brightness")
                        .setWhiteLedFileLocation("led:torch_0/brightness")
                        .setToggleFileLocation("led:switch_0/brightness")
        )
        // Sony
        deviceList.add(
                Device().setName("Xperia M2 Dual")
                        .setDeviceId("d2302")
                        .isDualTone(false)
                        .setSingleLedFileLocation("torch-light/brightness")
        )
        // YU
        deviceList.add(
                Device().setName("YU Yureka")
                        .setDeviceId("yureka")
                        .setAliases("tomato")
                        .isDualTone(false)
                        .setSingleLedFileLocation("torch-light0/brightness")
        )

        // ZUK
        deviceList.add(
                Device().setName("ZUK Z1")
                        .setDeviceId("z1")
                        .isDualTone(false)
                        .setSingleLedFileLocation("led:torch_0/brightness")
        )

        // Asus
        deviceList.add(
                Device().setName("Asus Zenfone Max")
                        .setDeviceId("ASUS_Z010D")
                        .isDualTone(false)
                        .setSingleLedFileLocation("torch-light0/brightness")
        )

        return deviceList
    }
}
