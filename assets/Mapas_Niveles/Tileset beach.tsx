<?xml version="1.0" encoding="UTF-8"?>
<tileset version="1.10" tiledversion="1.11.2" name="Tileset beach" tilewidth="32" tileheight="32" tilecount="256" columns="16">
 <image source="Tileset beach.png" width="512" height="512"/>
 <tile id="34">
  <objectgroup draworder="index" id="2">
   <object id="1" x="-0.131579" y="0" width="32.1316" height="32"/>
  </objectgroup>
 </tile>
 <tile id="54">
  <objectgroup draworder="index" id="2">
   <object id="4" x="-0.375" y="-0.375" width="32.875" height="32.5"/>
  </objectgroup>
 </tile>
 <wangsets>
  <wangset name="Arena con colision" type="mixed" tile="34">
   <wangcolor name="" color="#ff0000" tile="34" probability="1"/>
   <wangtile tileid="34" wangid="1,1,1,1,1,1,1,1"/>
  </wangset>
  <wangset name="Agua" type="mixed" tile="125">
   <wangcolor name="" color="#ff0000" tile="125" probability="1"/>
   <wangtile tileid="125" wangid="1,1,1,1,1,1,1,1"/>
  </wangset>
  <wangset name="arena" type="mixed" tile="34">
   <wangcolor name="" color="#ff0000" tile="34" probability="1"/>
   <wangtile tileid="33" wangid="1,1,1,1,1,1,1,1"/>
  </wangset>
  <wangset name="Bloque invisible" type="mixed" tile="54">
   <wangcolor name="" color="#ff0000" tile="54" probability="1"/>
   <wangtile tileid="54" wangid="1,1,1,1,1,1,1,1"/>
  </wangset>
 </wangsets>
</tileset>
