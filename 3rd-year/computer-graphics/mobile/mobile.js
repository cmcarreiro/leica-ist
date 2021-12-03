/*
    VARIAVEIS GLOBAIS
*/
var clock = new THREE.Clock();
var delta = 0;

var currCamera;
var frontCamera, topCamera, sideCamera;

var scene, renderer;
var geometry, material;
var mobile;
var mainAxis, secAxis, terAxis;

var wireMaterial = new THREE.MeshBasicMaterial({
  color: 0xEAEAEA,
  wireframe: true
});

var watermelonMaterial = new THREE.MeshBasicMaterial({
  color: 0xFF2B32,
  wireframe: true
});

melonCubeMaterial = new THREE.MeshBasicMaterial({
  color: 0x67C667,
  wireframe: true
});

cherryMaterial = new THREE.MeshBasicMaterial({
  color: 0xFF435D,
  wireframe: true
});

//flags
var swapMat = false;
var rotateMainAxis = 0;
var rotateSecAxis = 0;
var rotateTerAxis = 0;
var horizontalMov = 0;
var depthMov = 0;



/*
    OBJETOS
*/
function createMobile(x, y, z) {
  mobile = new THREE.Object3D();

  //criar eixos
  mainAxis = new THREE.Object3D();
  mainAxis.position.set(0, 0, 0);

  secAxis = new THREE.Object3D();
  secAxis.position.set(30, -30, 0);

  terAxis = new THREE.Object3D();
  terAxis.position.set(-40, -40, 0);


  //criar fios e frutas

  //mainAxis
  var wireZero = createWire(mainAxis, new THREE.Vector3(0, 0, 0), new THREE.Vector3(0, -30, 0));
  var wireOne = createWire(mainAxis, new THREE.Vector3(0, -30, 0), new THREE.Vector3(30, -30, 0));
  var wireTwo = createWire(mainAxis, new THREE.Vector3(0, -30, 0), new THREE.Vector3(-40, -50, 0));
  var wireThree = createWire(mainAxis, new THREE.Vector3(30, -30, 0), new THREE.Vector3(80, -25, 0));

  var watermelonZero = createWatermelon(mainAxis, new THREE.Vector3(-40, -50, 0));
  var watermelonOne = createWatermelon(mainAxis, new THREE.Vector3(80, -25, 0));

  //secAxis
  var wireFour = createWire(secAxis, new THREE.Vector3(0, 0, 0), new THREE.Vector3(0, -40, 0));
  var wireFive = createWire(secAxis, new THREE.Vector3(0, -40, 0), new THREE.Vector3(-40, -40, 0));
  var wireSix = createWire(secAxis, new THREE.Vector3(0, -40, 0), new THREE.Vector3(40, -60, 0));
  var wireSeven = createWire(secAxis, new THREE.Vector3(15, -47, 0), new THREE.Vector3(15, -70, 0));

  var melonCubeZero = createMelonCube(secAxis, new THREE.Vector3(40, -60, 0));
  var melonCubeOne = createMelonCube(secAxis, new THREE.Vector3(15, -70, 0));

  //terAxis
  var wireEight = createWire(terAxis, new THREE.Vector3(0, 0, 0), new THREE.Vector3(0, -60, 0));
  var wireNine = createWire(terAxis, new THREE.Vector3(-60, -60, 0), new THREE.Vector3(30, -60, 0));
  var wireTen = createWire(terAxis, new THREE.Vector3(-60, -60, 0), new THREE.Vector3(-60, -80, 0));
  var wireEleven = createWire(terAxis, new THREE.Vector3(-50, -60, 0), new THREE.Vector3(-50, -80, 0));
  var wireTwelve = createWire(terAxis, new THREE.Vector3(-20, -60, 0), new THREE.Vector3(-20, -90, 0));
  var wireThirteen = createWire(terAxis, new THREE.Vector3(-10, -60, 0), new THREE.Vector3(-10, -90, 0));
  var wireFourteen = createWire(terAxis, new THREE.Vector3(20, -60, 0), new THREE.Vector3(20, -100, 0));
  var wireFifteen = createWire(terAxis, new THREE.Vector3(30, -60, 0), new THREE.Vector3(30, -100, 0));

  var cherryZero = createCherry(terAxis, new THREE.Vector3(-60, -80, 0));
  var cherryOne = createCherry(terAxis, new THREE.Vector3(-50, -80, 0));
  var cherryTwo = createCherry(terAxis, new THREE.Vector3(-20, -90, 0));
  var cherryThree = createCherry(terAxis, new THREE.Vector3(-10, -90, 0));
  var cherryFour = createCherry(terAxis, new THREE.Vector3(20, -100, 0));
  var cherryFive = createCherry(terAxis, new THREE.Vector3(30, -100, 0));

  //adicionar objetos
  secAxis.add(terAxis);
  mainAxis.add(secAxis);
  mobile.add(mainAxis);

  mobile.position.set(x, y, z);
  scene.add(mobile);
}

function createWire(obj, pointA, pointB) {
  let h = pointA.distanceTo(pointB);
  geometry = new THREE.CylinderGeometry(0.5, 0.5, h, 20); //raio da base topo, raio da base baixo, altura, faces
  mesh = new THREE.Mesh(geometry, wireMaterial);
  mesh.position.set((pointA.x + pointB.x) / 2, (pointA.y + pointB.y) / 2, pointA.z);
  let dirVector = pointB.sub(pointA);
  mesh.rotateZ(Math.atan2(dirVector.y, dirVector.x) - Math.PI / 2);
  obj.add(mesh);
}

function createWatermelon(obj, pointTop) {
  geometry = new THREE.CylinderGeometry(18, 18, 5, 20); //raio da base topo, raio da base baixo, altura, faces
  mesh = new THREE.Mesh(geometry, watermelonMaterial);
  mesh.rotateX(Math.PI / 2);
  mesh.scale.setX(1.5);
  mesh.position.set(pointTop.x, pointTop.y - 18, 0);
  obj.add(mesh);
}

function createMelonCube(obj, pointTop) {
  geometry = new THREE.BoxGeometry(20, 20, 5); //width, height, depth
  mesh = new THREE.Mesh(geometry, melonCubeMaterial);
  mesh.position.set(pointTop.x, pointTop.y - 10, 0);
  obj.add(mesh);
}

function createCherry(obj, pointTop) {
  geometry = new THREE.CylinderGeometry(4.5, 4.5, 5, 10); //raio da base topo, raio da base baixo, altura, faces
  mesh = new THREE.Mesh(geometry, cherryMaterial);
  mesh.rotateX(Math.PI / 2);
  mesh.position.set(pointTop.x, pointTop.y - 4.5, 0);
  obj.add(mesh);
}



/*
    CENA E CAMARA
*/
function createScene() {
  'use strict';
  scene = new THREE.Scene();
  createMobile(0, 90, 0);
}


function createCamera(x, y, z) {
  'use strict';
  const aspect = window.innerWidth / window.innerHeight;
  const frustumSize = 250;
  let camera = new THREE.OrthographicCamera(frustumSize * aspect / -2, frustumSize * aspect / 2, frustumSize / 2, frustumSize / -2, 1, 1000); //change values

  //set camera position
  camera.position.x = x; //vermelho
  camera.position.y = y; //verde
  camera.position.z = z; //azul

  //set where camera looks at
  camera.lookAt(scene.position);
  return camera;
}



/*
    EVENTOS
*/
function onKeyDown(e) {
  switch (e.keyCode) {
    case 49: //1
      currCamera = frontCamera;
      break;
    case 50: //2
      currCamera = topCamera;
      break;
    case 51: //3
      currCamera = sideCamera;
      break;
    case 52: //4
      swapMat = true;
      break;
    case 81: //q
      rotateMainAxis = 1;
      break;
    case 87: //w
      rotateMainAxis = -1;
      break;
    case 65: //a
      rotateSecAxis = 1;
      break;
    case 68: //d
      rotateSecAxis = -1;
      break;
    case 90: //z
      rotateTerAxis = 1;
      break;
    case 67: //c
      rotateTerAxis = -1;
      break;
    case 37: //left
      horizontalMov = -1;
      break;
    case 39: //right
      horizontalMov = 1;
      break;
    case 38: //up
      depthMov = -1;
      break;
    case 40: //down
      depthMov = 1;
      break;
  }
}

function onKeyUp(e) {
  switch (e.keyCode) {
    case 81: //q
      rotateMainAxis = 0;
      break;
    case 87: //w
      rotateMainAxis = 0;
      break;
    case 65: //a
      rotateSecAxis = 0;
      break;
    case 68: //d
      rotateSecAxis = 0;
      break;
    case 90: //z
      rotateTerAxis = 0;
      break;
    case 67: //c
      rotateTerAxis = 0;
      break;
    case 37: //left
      horizontalMov = 0;
      break;
    case 39: //right
      horizontalMov = 0;
      break;
    case 38: //up
      depthMov = 0;
      break;
    case 40: //down
      depthMov = 0;
      break;
  }
}


function onResize() {
  'use strict';
  currCamera.aspect = window.innerWidth / window.innerHeight;
  currCamera.updateProjectionMatrix();
  renderer.setSize(window.innerWidth, window.innerHeight);
}



/*
    ANIMACAO
*/
function render() {
  'use strict';
  renderer.render(scene, currCamera);
}

function animate() {
  delta = clock.getDelta();

  //tratar flags
  if (swapMat) {
    wireMaterial.wireframe = !wireMaterial.wireframe;
    watermelonMaterial.wireframe = !watermelonMaterial.wireframe;
    melonCubeMaterial.wireframe = !melonCubeMaterial.wireframe;
    cherryMaterial.wireframe = !cherryMaterial.wireframe;
    swapMat = false;
  }

  mainAxis.rotation.y += 1 * rotateMainAxis * delta;
  secAxis.rotation.y += 2 * rotateSecAxis * delta;
  terAxis.rotation.y += 3 * rotateTerAxis * delta;

  mobile.position.x += 1 * horizontalMov;
  mobile.position.z += 1 * depthMov;

  render();
  requestAnimationFrame(animate);
}

function init() {
  'use strict';
  renderer = new THREE.WebGLRenderer({
    antialias: true
  });
  renderer.setSize(window.innerWidth, window.innerHeight);
  document.body.appendChild(renderer.domElement);

  createScene();

  currCamera = frontCamera = createCamera(0, 0, 200); //default camera is frontal
  topCamera = createCamera(0, 200, 0);
  sideCamera = createCamera(200, 0, 0);

  render();

  window.addEventListener("resize", onResize);
  window.addEventListener("keydown", onKeyDown);
  window.addEventListener("keyup", onKeyUp);
}