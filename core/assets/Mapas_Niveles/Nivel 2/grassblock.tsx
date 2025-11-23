<?xml version="1.0" encoding="UTF-8"?>
<tileset version="1.10" tiledversion="1.11.2" name="grassblock" tilewidth="32" tileheight="32" tilecount="4" columns="2">
 <image source="grassblock.png" width="64" height="64"/>
 <tile id="1">
  <objectgroup draworder="index" id="2">
   <object id="1" x="0.727273" y="0.545455" width="30.3636" height="30.7273"/>
  </objectgroup>
 </tile>
 <tile id="2">
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="0" width="32" height="32"/>
  </objectgroup>
 </tile>
 <wangsets>
  <wangset name="grassblock" type="mixed" tile="0">
   <wangcolor name="" color="#ff0000" tile="0" probability="1"/>
   <wangtile tileid="0" wangid="1,1,1,1,1,1,1,1"/>
  </wangset>
  <wangset name="dirtblock" type="mixed" tile="2">
   <wangcolor name="" color="#ff0000" tile="2" probability="1"/>
   <wangtile tileid="2" wangid="1,1,1,1,1,1,1,1"/>
  </wangset>
  <wangset name="cesped colision" type="mixed" tile="1">
   <wangcolor name="" color="#ff0000" tile="1" probability="1"/>
   <wangtile tileid="1" wangid="1,1,1,1,1,1,1,1"/>
  </wangset>
  <wangset name="dirtblock con colision" type="mixed" tile="2">
   <wangcolor name="" color="#ff0000" tile="2" probability="1"/>
   <wangtile tileid="2" wangid="1,1,1,1,1,1,1,1"/>
  </wangset>
 </wangsets>
</tileset>
