"""
 Copyright 2020 Tianshu AI Platform. All Rights Reserved.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 =============================================================
"""


import os
from glob import glob
from PIL import Image
from .utils import colormap
from torchvision.datasets import VisionDataset

class SunRGBD(VisionDataset):
    cmap = colormap()
    def __init__(self,
                 root,
                 split='train',
                 transform=None,
                 target_transform=None,
                 transforms=None):
        super( SunRGBD, self ).__init__( root, transform=transform, target_transform=target_transform, transforms=transforms )
        self.root = root
        self.split = split

        self.images = glob(os.path.join(self.root, 'SUNRGBD-%s_images'%self.split, '*.jpg'))
        self.labels = glob(os.path.join(self.root, '%s13labels'%self.split, '*.png'))

        self.images.sort()
        self.labels.sort()

    def __getitem__(self, idx):
        """
        Args:
        - index (``int``): index of the item in the dataset
        Returns:
        A tuple of ``PIL.Image`` (image, label) where label is the ground-truth
        of the image.
        """

        img, label = Image.open(self.images[idx]), Image.open(self.labels[idx])

        if self.transform is not None:
            img, label = self.transform(img, label)
        label = label-1  # void 0=>255
        return img, label

    def __len__(self):
        return len(self.images)

    @classmethod
    def decode_fn(cls, mask):
        """decode semantic mask to RGB image"""
        return cls.cmap[mask.astype('uint8')+1]