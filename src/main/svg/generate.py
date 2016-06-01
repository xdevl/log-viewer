# Copyright (C) 2016 XdevL
#
# This file is part of Log viewer.

# Log viewer is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.

# Log viewer is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.

# You should have received a copy of the GNU General Public License
# along with Log viewer. If not, see <http://www.gnu.org/licenses/>.

import os
import subprocess

# Comment out screen densities your application doesn't support
DENSITIES=[
	("mdpi",1),
	("hdpi",1.5),
	("xhdpi",2),
	("xxhdpi",3),
	("xxxhdpi",4)
]

SVG_DIRECTORY=os.path.dirname(os.path.realpath(__file__))
RES_DIRECTORY=os.path.join(SVG_DIRECTORY,os.pardir,"res")

# Generate PNG images for all the defined densities from a SVG file
def generate(suffix, size, svgs):
	for svg in svgs:
		for density in DENSITIES:
			dst=os.path.join(RES_DIRECTORY,"drawable-%s"%(density[0]))
			if not os.path.exists(dst):
				os.makedirs(dst)
			subprocess.check_call([
				"inkscape",
				"--export-area-page",
				"--export-width",str(density[1]*size),
				"--export-png",os.path.join(dst,"%s%s.png"%(suffix,svg)),
				os.path.join(SVG_DIRECTORY,"%s.svg"%(svg))])
		print("Generated %s"%(svg))

# Application icon
generate("ic_",48,[
	"logviewer"])

# Menu and contextual icons
generate("ic_action_",24,[
	"search",
	"up",
	"down",
	"filter"])

# List item icons
generate("ic_",40,[
	"android",
	"linux",
	"about"])
	
