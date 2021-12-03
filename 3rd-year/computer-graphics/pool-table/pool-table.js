/*
  CONSTANTES
*/
const TABLE_WIDTH = 100;
const TABLE_LENGTH = 200;
const TABLE_HEIGHT = 20;

const LEG_SIDE = 20;
const LEG_HEIGHT = 60;

const BALL_DIAMETER = 5;
const BALL_WALL_DISTANCE = 20;
const BALL_COLOR_NUMBER = 15;
const BALL_ACCELERATION = 10;

const WALL_WIDTH = 10;
const WALL_SMALL_LENGTH = TABLE_WIDTH;
const WALL_BIG_LENGHT = TABLE_LENGTH + 2 * WALL_WIDTH;
const WALL_HEIGHT = 2 * BALL_DIAMETER;

const COVER_HEIGHT = 1;

const CUE_SMALL_RADIUS = 0.6;
const CUE_BIG_RADIUS = 1.5;
const CUE_LENGTH = 150;
const CUE_ANGLE = 5 * Math.PI / 180;
const CUE_X_OFFSET = CUE_LENGTH / 2 - CUE_LENGTH / 2 * Math.cos(CUE_ANGLE);
const CUE_Y_OFFSET = CUE_LENGTH / 2 * Math.sin(CUE_ANGLE);
const CUE_MAX_ANGLE = 60 * Math.PI / 180;

const SHOT_POWER = 60;

const HOLE_DIAMETER = BALL_DIAMETER * 1.5;

const FALL_SPEED = 50;


/*
    VARIAVEIS GLOBAIS
*/
var clock = new THREE.Clock();

var currentCamera;
var topCamera, perspCamera, ballCamera;

var scene, renderer;
var poolTable = new THREE.Object3D();

var cuesPivots = [];
var whiteBallsPivots = [];
var collisionBallsGeometryPivots = [];
var collisionBallsSpheres = [];
var holesSpheres = [];

var rotateCue = 0;
var selectedCue = 0;
var newCue = false;
var newShot = false;

var cueDefaultMaterial = new THREE.MeshBasicMaterial({ color: 0x7F3300 });
var cueSelectedMaterial = new THREE.MeshBasicMaterial({ color: 0x3EC5F2 });


/*
    OBJETOS
*/
function createTable() {
  let lightBrownMaterial = new THREE.MeshBasicMaterial({ color: 0xcc6600 });
  let darkGreenMaterial = new THREE.MeshBasicMaterial({ color: 0x009900 });
  let blackMaterial = new THREE.MeshBasicMaterial({ color: 0x000000 });
  let edgeMaterial = new THREE.LineBasicMaterial({ color: 0xffff00, linewidth: 1 });

  let tableLeg1 = new THREE.Mesh(new THREE.BoxGeometry(LEG_SIDE, LEG_HEIGHT, LEG_SIDE), lightBrownMaterial)
  tableLeg1.add(new THREE.LineSegments(new THREE.EdgesGeometry(tableLeg1.geometry), edgeMaterial));
  tableLeg1.position.set(TABLE_LENGTH / 2 - LEG_SIDE / 2, -LEG_HEIGHT / 2 - TABLE_HEIGHT / 2, TABLE_WIDTH / 2 - LEG_SIDE / 2);

  let tableLeg2 = new THREE.Mesh(new THREE.BoxGeometry(LEG_SIDE, LEG_HEIGHT, LEG_SIDE), lightBrownMaterial)
  tableLeg2.add(new THREE.LineSegments(new THREE.EdgesGeometry(tableLeg2.geometry), edgeMaterial));
  tableLeg2.position.set(TABLE_LENGTH / 2 - LEG_SIDE / 2, -LEG_HEIGHT / 2 - TABLE_HEIGHT / 2, -TABLE_WIDTH / 2 + LEG_SIDE / 2);

  let tableLeg3 = new THREE.Mesh(new THREE.BoxGeometry(LEG_SIDE, LEG_HEIGHT, LEG_SIDE), lightBrownMaterial)
  tableLeg3.add(new THREE.LineSegments(new THREE.EdgesGeometry(tableLeg3.geometry), edgeMaterial));
  tableLeg3.position.set(-TABLE_LENGTH / 2 + LEG_SIDE / 2, -LEG_HEIGHT / 2 - TABLE_HEIGHT / 2, -TABLE_WIDTH / 2 + LEG_SIDE / 2);

  let tableLeg4 = new THREE.Mesh(new THREE.BoxGeometry(LEG_SIDE, LEG_HEIGHT, LEG_SIDE), lightBrownMaterial)
  tableLeg4.add(new THREE.LineSegments(new THREE.EdgesGeometry(tableLeg4.geometry), edgeMaterial));
  tableLeg4.position.set(-TABLE_LENGTH / 2 + LEG_SIDE / 2, -LEG_HEIGHT / 2 - TABLE_HEIGHT / 2, TABLE_WIDTH / 2 - LEG_SIDE / 2);


  let tableTop = new THREE.Mesh(new THREE.BoxGeometry(TABLE_LENGTH, TABLE_HEIGHT - COVER_HEIGHT, TABLE_WIDTH), lightBrownMaterial);
  tableTop.add(new THREE.LineSegments(new THREE.EdgesGeometry(tableTop.geometry), edgeMaterial));
  tableTop.position.set(0, -COVER_HEIGHT / 2, 0);

  let tableCover = new THREE.Mesh(new THREE.BoxGeometry(TABLE_LENGTH, COVER_HEIGHT, TABLE_WIDTH), darkGreenMaterial);
  tableCover.position.set(0, TABLE_HEIGHT / 2 - COVER_HEIGHT / 2, 0);


  let tableWall1 = new THREE.Mesh(new THREE.BoxGeometry(WALL_WIDTH, WALL_HEIGHT, WALL_SMALL_LENGTH), lightBrownMaterial);
  tableWall1.add(new THREE.LineSegments(new THREE.EdgesGeometry(tableWall1.geometry), edgeMaterial));
  tableWall1.position.set(TABLE_LENGTH / 2 + WALL_WIDTH / 2, TABLE_HEIGHT / 2 + WALL_HEIGHT / 2, 0);

  let tableWall2 = new THREE.Mesh(new THREE.BoxGeometry(WALL_BIG_LENGHT, WALL_HEIGHT, WALL_WIDTH), lightBrownMaterial);
  tableWall2.add(new THREE.LineSegments(new THREE.EdgesGeometry(tableWall2.geometry), edgeMaterial));
  tableWall2.position.set(0, TABLE_HEIGHT / 2 + WALL_HEIGHT / 2, -TABLE_WIDTH / 2 - WALL_WIDTH / 2);

  let tableWall3 = new THREE.Mesh(new THREE.BoxGeometry(WALL_WIDTH, WALL_HEIGHT, WALL_SMALL_LENGTH), lightBrownMaterial);
  tableWall3.add(new THREE.LineSegments(new THREE.EdgesGeometry(tableWall3.geometry), edgeMaterial));
  tableWall3.position.set(-TABLE_LENGTH / 2 - WALL_WIDTH / 2, TABLE_HEIGHT / 2 + WALL_HEIGHT / 2, 0);

  let tableWall4 = new THREE.Mesh(new THREE.BoxGeometry(WALL_BIG_LENGHT, WALL_HEIGHT, WALL_WIDTH), lightBrownMaterial);
  tableWall4.add(new THREE.LineSegments(new THREE.EdgesGeometry(tableWall4.geometry), edgeMaterial));
  tableWall4.position.set(0, TABLE_HEIGHT / 2 + WALL_HEIGHT / 2, TABLE_WIDTH / 2 + WALL_WIDTH / 2);


  let hole1 = new THREE.Mesh(new THREE.CylinderGeometry(HOLE_DIAMETER / 2, HOLE_DIAMETER / 2, COVER_HEIGHT, 16), blackMaterial);
  hole1.position.set(TABLE_LENGTH / 2 - HOLE_DIAMETER / 2, TABLE_HEIGHT / 2 - COVER_HEIGHT / 2 + 0.1, -TABLE_WIDTH / 2 + HOLE_DIAMETER / 2);
  holesSpheres.push(new THREE.Sphere(new THREE.Vector3(TABLE_LENGTH / 2 - HOLE_DIAMETER / 2, TABLE_HEIGHT / 2 + BALL_DIAMETER, -TABLE_WIDTH / 2 + HOLE_DIAMETER / 2), HOLE_DIAMETER / 2 - BALL_DIAMETER / 2));

  let hole2 = new THREE.Mesh(new THREE.CylinderGeometry(HOLE_DIAMETER / 2, HOLE_DIAMETER / 2, COVER_HEIGHT, 16), blackMaterial);
  hole2.position.set(0, TABLE_HEIGHT / 2 - COVER_HEIGHT / 2 + 0.1, -TABLE_WIDTH / 2 + HOLE_DIAMETER / 2);
  holesSpheres.push(new THREE.Sphere(new THREE.Vector3(0, TABLE_HEIGHT / 2 + BALL_DIAMETER, -TABLE_WIDTH / 2 + HOLE_DIAMETER / 2), HOLE_DIAMETER / 2 - BALL_DIAMETER / 2));

  let hole3 = new THREE.Mesh(new THREE.CylinderGeometry(HOLE_DIAMETER / 2, HOLE_DIAMETER / 2, COVER_HEIGHT, 16), blackMaterial);
  hole3.position.set(-TABLE_LENGTH / 2 + HOLE_DIAMETER / 2, TABLE_HEIGHT / 2 - COVER_HEIGHT / 2 + 0.1, -TABLE_WIDTH / 2 + HOLE_DIAMETER / 2);
  holesSpheres.push(new THREE.Sphere(new THREE.Vector3(-TABLE_LENGTH / 2 + HOLE_DIAMETER / 2, TABLE_HEIGHT / 2 + BALL_DIAMETER, -TABLE_WIDTH / 2 + HOLE_DIAMETER / 2), HOLE_DIAMETER / 2 - BALL_DIAMETER / 2));

  let hole4 = new THREE.Mesh(new THREE.CylinderGeometry(HOLE_DIAMETER / 2, HOLE_DIAMETER / 2, COVER_HEIGHT, 16), blackMaterial);
  hole4.position.set(-TABLE_LENGTH / 2 + HOLE_DIAMETER / 2, TABLE_HEIGHT / 2 - COVER_HEIGHT / 2 + 0.1, TABLE_WIDTH / 2 - HOLE_DIAMETER / 2);
  holesSpheres.push(new THREE.Sphere(new THREE.Vector3(-TABLE_LENGTH / 2 + HOLE_DIAMETER / 2, TABLE_HEIGHT / 2 + BALL_DIAMETER, TABLE_WIDTH / 2 - HOLE_DIAMETER / 2), HOLE_DIAMETER / 2 - BALL_DIAMETER / 2));

  let hole5 = new THREE.Mesh(new THREE.CylinderGeometry(HOLE_DIAMETER / 2, HOLE_DIAMETER / 2, COVER_HEIGHT, 16), blackMaterial);
  hole5.position.set(0, TABLE_HEIGHT / 2 - COVER_HEIGHT / 2 + 0.1, TABLE_WIDTH / 2 - HOLE_DIAMETER / 2);
  holesSpheres.push(new THREE.Sphere(new THREE.Vector3(0, TABLE_HEIGHT / 2 + BALL_DIAMETER, TABLE_WIDTH / 2 - HOLE_DIAMETER / 2), HOLE_DIAMETER / 2 - BALL_DIAMETER / 2));

  let hole6 = new THREE.Mesh(new THREE.CylinderGeometry(HOLE_DIAMETER / 2, HOLE_DIAMETER / 2, COVER_HEIGHT, 16), blackMaterial);
  hole6.position.set(TABLE_LENGTH / 2 - HOLE_DIAMETER / 2, TABLE_HEIGHT / 2 - COVER_HEIGHT / 2 + 0.1, TABLE_WIDTH / 2 - HOLE_DIAMETER / 2);
  holesSpheres.push(new THREE.Sphere(new THREE.Vector3(TABLE_LENGTH / 2 - HOLE_DIAMETER / 2, TABLE_HEIGHT / 2 + BALL_DIAMETER, TABLE_WIDTH / 2 - HOLE_DIAMETER / 2), HOLE_DIAMETER / 2 - BALL_DIAMETER / 2));


  poolTable.add(tableLeg1);
  poolTable.add(tableLeg2);
  poolTable.add(tableLeg3);
  poolTable.add(tableLeg4);

  poolTable.add(tableTop);
  poolTable.add(tableCover);

  poolTable.add(tableWall1);
  poolTable.add(tableWall2);
  poolTable.add(tableWall3);
  poolTable.add(tableWall4);

  poolTable.add(hole1);
  poolTable.add(hole2);
  poolTable.add(hole3);
  poolTable.add(hole4);
  poolTable.add(hole5);
  poolTable.add(hole6);

  poolTable.position.set(0, -TABLE_HEIGHT / 2 - BALL_DIAMETER / 2, 0);
}

function createCues() {

  let cues = [
    {
      "pos_cue": new THREE.Vector3(CUE_LENGTH / 2 + BALL_DIAMETER / 2 - CUE_X_OFFSET, CUE_Y_OFFSET, 0),
      "rot": -Math.PI / 2 + CUE_ANGLE,
      "pos_pivot": new THREE.Vector3(TABLE_LENGTH / 2 - BALL_DIAMETER / 2 - BALL_WALL_DISTANCE, TABLE_HEIGHT / 2 + BALL_DIAMETER / 2, 0)
    },
    {
      "pos_cue": new THREE.Vector3(0, CUE_Y_OFFSET, -CUE_LENGTH / 2 + CUE_X_OFFSET),
      "rot": -Math.PI / 2 + CUE_ANGLE,
      "pos_pivot": new THREE.Vector3(TABLE_LENGTH / 6, TABLE_HEIGHT / 2 + BALL_DIAMETER / 2, -TABLE_WIDTH / 2 + BALL_DIAMETER / 2 + BALL_WALL_DISTANCE)
    },
    {
      "pos_cue": new THREE.Vector3(0, CUE_Y_OFFSET, -CUE_LENGTH / 2 + CUE_X_OFFSET),
      "rot": -Math.PI / 2 + CUE_ANGLE,
      "pos_pivot": new THREE.Vector3(-TABLE_LENGTH / 6, TABLE_HEIGHT / 2 + BALL_DIAMETER / 2, -TABLE_WIDTH / 2 + BALL_DIAMETER / 2 + BALL_WALL_DISTANCE)
    },
    {
      "pos_cue": new THREE.Vector3(-CUE_LENGTH / 2 + CUE_X_OFFSET, CUE_Y_OFFSET, 0),
      "rot": Math.PI / 2 - CUE_ANGLE,
      "pos_pivot": new THREE.Vector3(-TABLE_LENGTH / 2 + BALL_DIAMETER / 2 + BALL_WALL_DISTANCE, TABLE_HEIGHT / 2 + BALL_DIAMETER / 2, 0)
    },
    {
      "pos_cue": new THREE.Vector3(0, CUE_Y_OFFSET, CUE_LENGTH / 2 - CUE_X_OFFSET),
      "rot": Math.PI / 2 - CUE_ANGLE,
      "pos_pivot": new THREE.Vector3(-TABLE_LENGTH / 6, TABLE_HEIGHT / 2 + BALL_DIAMETER / 2, TABLE_WIDTH / 2 - BALL_DIAMETER / 2 - BALL_WALL_DISTANCE)
    },
    {
      "pos_cue": new THREE.Vector3(0, CUE_Y_OFFSET, CUE_LENGTH / 2 - CUE_X_OFFSET),
      "rot": Math.PI / 2 - CUE_ANGLE,
      "pos_pivot": new THREE.Vector3(TABLE_LENGTH / 6, TABLE_HEIGHT / 2 + BALL_DIAMETER / 2, TABLE_WIDTH / 2 - BALL_DIAMETER / 2 - BALL_WALL_DISTANCE)
    }
  ];

  for (let i = 0; i < 6; i++) {

    let cue = new THREE.Mesh(new THREE.CylinderGeometry(CUE_BIG_RADIUS, CUE_SMALL_RADIUS, CUE_LENGTH), cueDefaultMaterial);
    let pivot = new THREE.Group();

    cue.position.copy(cues[i].pos_cue);
    pivot.position.copy(cues[i].pos_pivot);

    if (i == 0 || i == 3)
      cue.rotateZ(cues[i].rot)
    else
      cue.rotateX(cues[i].rot)

    pivot.add(cue);
    cuesPivots.push(pivot);
    poolTable.add(pivot);
  }
}

function createWhiteBalls() {
  let whiteMaterial = new THREE.MeshBasicMaterial({ color: 0xFFFFFF });

  let ball1 = new THREE.Mesh(new THREE.SphereGeometry(BALL_DIAMETER / 2, 16, 16), whiteMaterial);
  ball1.add(new THREE.AxesHelper(10));
  let pivot1 = new THREE.Group();
  pivot1.add(ball1);
  pivot1.falling = false;
  pivot1.position.set(TABLE_LENGTH / 2 - BALL_DIAMETER / 2 - BALL_WALL_DISTANCE, TABLE_HEIGHT / 2 + BALL_DIAMETER / 2, 0);
  pivot1.velocity = new THREE.Vector3(-1, 0, 0);
  pivot1.offset = new THREE.Vector3(15, 20, 0);
  alignInnerBallXWithVelocity(pivot1);
  whiteBallsPivots.push(pivot1);

  let ball2 = new THREE.Mesh(new THREE.SphereGeometry(BALL_DIAMETER / 2, 16, 16), whiteMaterial);
  ball2.add(new THREE.AxesHelper(10));
  let pivot2 = new THREE.Group();
  pivot2.add(ball2);
  pivot2.falling = false;
  pivot2.position.set(TABLE_LENGTH / 6, TABLE_HEIGHT / 2 + BALL_DIAMETER / 2, -TABLE_WIDTH / 2 + BALL_DIAMETER / 2 + BALL_WALL_DISTANCE);
  pivot2.velocity = new THREE.Vector3(0, 0, 1);
  pivot2.offset = new THREE.Vector3(0, 20, -15);
  alignInnerBallXWithVelocity(pivot2);
  whiteBallsPivots.push(pivot2);

  let ball3 = new THREE.Mesh(new THREE.SphereGeometry(BALL_DIAMETER / 2, 16, 16), whiteMaterial);
  ball3.add(new THREE.AxesHelper(10));
  let pivot3 = new THREE.Group();
  pivot3.add(ball3);
  pivot3.falling = false;
  pivot3.position.set(-TABLE_LENGTH / 6, TABLE_HEIGHT / 2 + BALL_DIAMETER / 2, -TABLE_WIDTH / 2 + BALL_DIAMETER / 2 + BALL_WALL_DISTANCE);
  pivot3.velocity = new THREE.Vector3(0, 0, 1);
  pivot3.offset = new THREE.Vector3(0, 20, -15);
  alignInnerBallXWithVelocity(pivot3);
  whiteBallsPivots.push(pivot3);

  let ball4 = new THREE.Mesh(new THREE.SphereGeometry(BALL_DIAMETER / 2, 16, 16), whiteMaterial);
  ball4.add(new THREE.AxesHelper(10));
  let pivot4 = new THREE.Group();
  pivot4.add(ball4);
  pivot4.falling = false;
  pivot4.position.set(-TABLE_LENGTH / 2 + BALL_DIAMETER / 2 + BALL_WALL_DISTANCE, TABLE_HEIGHT / 2 + BALL_DIAMETER / 2, 0);
  pivot4.velocity = new THREE.Vector3(1, 0, 0);
  pivot4.offset = new THREE.Vector3(-15, 20, 0);
  alignInnerBallXWithVelocity(pivot4);
  whiteBallsPivots.push(pivot4);

  let ball5 = new THREE.Mesh(new THREE.SphereGeometry(BALL_DIAMETER / 2, 16, 16), whiteMaterial);

  ball5.add(new THREE.AxesHelper(10));
  let pivot5 = new THREE.Group();
  pivot5.add(ball5);
  pivot5.falling = false;
  pivot5.position.set(-TABLE_LENGTH / 6, TABLE_HEIGHT / 2 + BALL_DIAMETER / 2, TABLE_WIDTH / 2 - BALL_DIAMETER / 2 - BALL_WALL_DISTANCE);
  pivot5.velocity = new THREE.Vector3(0, 0, -1);
  pivot5.offset = new THREE.Vector3(0, 20, 15);
  alignInnerBallXWithVelocity(pivot5);
  whiteBallsPivots.push(pivot5);

  let ball6 = new THREE.Mesh(new THREE.SphereGeometry(BALL_DIAMETER / 2, 16, 16), whiteMaterial);
  ball6.add(new THREE.AxesHelper(10));
  let pivot6 = new THREE.Group();
  pivot6.add(ball6);
  pivot6.falling = false;
  pivot6.position.set(TABLE_LENGTH / 6, TABLE_HEIGHT / 2 + BALL_DIAMETER / 2, TABLE_WIDTH / 2 - BALL_DIAMETER / 2 - BALL_WALL_DISTANCE);
  pivot6.velocity = new THREE.Vector3(0, 0, -1);
  pivot6.offset = new THREE.Vector3(0, 20, 15);
  alignInnerBallXWithVelocity(pivot6);
  whiteBallsPivots.push(pivot6);

  for (let i = 0; i < 6; i++) { poolTable.add(whiteBallsPivots[i]); }
}

function createColorBalls() {
  let x, z, directionX, directionY, velocity;
  for (let i = 0; i < BALL_COLOR_NUMBER; i++) {
    let redMaterial = new THREE.MeshBasicMaterial({ color: 0xFF0000 });
    let innerBall = new THREE.Mesh(new THREE.SphereGeometry(BALL_DIAMETER / 2, 16, 16), redMaterial);
    innerBall.add(new THREE.AxesHelper(10));
    let temporaryPivot = new THREE.Group();
    temporaryPivot.falling = false;
    temporaryPivot.add(innerBall);
    temporaryPivot.velocity = new THREE.Vector3(THREE.MathUtils.randFloat(-100, 100), 0, THREE.MathUtils.randFloat(-100, 100));

    alignInnerBallXWithVelocity(temporaryPivot);

    innerBall.geometry.computeBoundingSphere();
    collisionBallsGeometryPivots.push(temporaryPivot);
    collisionBallsSpheres.push(new THREE.Sphere(temporaryPivot.position, innerBall.geometry.boundingSphere.radius));
    x = THREE.MathUtils.randFloat(-TABLE_LENGTH / 2 + BALL_DIAMETER / 2, TABLE_LENGTH / 2 - BALL_DIAMETER / 2);
    z = THREE.MathUtils.randFloat(-TABLE_WIDTH / 2 + BALL_DIAMETER / 2, TABLE_WIDTH / 2 - BALL_DIAMETER / 2);
    collisionBallsSpheres[i].center.set(x, TABLE_HEIGHT / 2 + BALL_DIAMETER / 2, z); //pivot also gets changed

    while (hasCollisionOtherBalls(i) != -1) {
      x = THREE.MathUtils.randFloat(-TABLE_LENGTH / 2 + BALL_DIAMETER / 2, TABLE_LENGTH / 2 - BALL_DIAMETER / 2);
      z = THREE.MathUtils.randFloat(-TABLE_WIDTH / 2 + BALL_DIAMETER / 2, TABLE_WIDTH / 2 - BALL_DIAMETER / 2);
      collisionBallsSpheres[i].center.set(x, TABLE_HEIGHT / 2 + BALL_DIAMETER / 2, z); //pivot also gets changed
    }
    poolTable.add(collisionBallsGeometryPivots[i]);
  }
}


/*
  FUNCOES AUXILIARES
*/
function hasCollisionOtherBalls(ballIndex) {
  for (let i = 0; i < collisionBallsSpheres.length; i++)
    if (i != ballIndex && collisionBallsSpheres[ballIndex].intersectsSphere(collisionBallsSpheres[i]))
      return i
  return -1;
}

function alignInnerBallXWithVelocity(pivot) {
  innerBall = pivot.children[0];
  innerBall.rotation.set(0, 0, 0);
  if (pivot.velocity.z <= 0)
    innerBall.rotation.y = pivot.velocity.angleTo(new THREE.Vector3(1, 0, 0));
  else if (pivot.velocity.z > 0)
    innerBall.rotation.y = -pivot.velocity.angleTo(new THREE.Vector3(1, 0, 0));
}

function checkIfBallsHole() {
  for (let i = 0; i < collisionBallsGeometryPivots.length; i++) {
    let sphere = collisionBallsSpheres[i];
    let ball = collisionBallsGeometryPivots[i];
    for (let j = 0; j < holesSpheres.length; j++)
      if (sphere.intersectsSphere(holesSpheres[j]))
        ball.falling = true;
  }
}


/*
    CENA E CAMARAS
*/
function createScene() {
  let axesHelper = new THREE.AxesHelper(200);

  createTable();
  createCues();
  createWhiteBalls();
  createColorBalls();

  scene = new THREE.Scene();
  scene.add(axesHelper);
  scene.add(poolTable);
}

function createTopCamera(x, y, z) {
  const aspect = window.innerWidth / window.innerHeight;
  const frustumSize = 200;
  let camera = new THREE.OrthographicCamera(frustumSize * aspect / -2, frustumSize * aspect / 2, frustumSize / 2, frustumSize / -2, 1, 1000); //change values

  camera.position.x = x;
  camera.position.y = y;
  camera.position.z = z;

  //set where camera looks at
  camera.lookAt(scene.position);
  return camera;
}

function createPerspectiveCamera(x, y, z) {
  let fov = 45;
  let aspect = window.innerWidth / window.innerHeight;
  let near = 1;
  let far = 1000;

  let camera = new THREE.PerspectiveCamera(fov, aspect, near, far);

  camera.position.x = x;
  camera.position.y = y;
  camera.position.z = z;

  camera.lookAt(scene.position);
  return camera;
}

function createBallCamera(pivot) {
  let fov = 70;
  let aspect = window.innerWidth / window.innerHeight;
  let near = 1;
  let far = 1000;

  let camera = new THREE.PerspectiveCamera(fov, aspect, near, far);

  camera.position.x = pivot.offset.x;
  camera.position.y = pivot.offset.y;
  camera.position.z = pivot.offset.z;

  camera.rotateZ(Math.PI / 4);
  camera.lookAt(scene.position);
  return camera;
}


/*
    EVENTOS
*/
function onKeyDown(e) {
  switch (e.keyCode) {
    case 49: //1
      currentCamera = topCamera;
      break;
    case 50: //2
      currentCamera = perspectiveCamera;
      break;
    case 51: //3
      currentCamera = ballCamera;
      break;
    case 52: //4
      if (selectedCue != 1) newCue = true;
      selectedCue = 1;
      break;
    case 53: //5
      if (selectedCue != 2) newCue = true;
      selectedCue = 2;
      break;
    case 54: //6
      if (selectedCue != 3) newCue = true;
      selectedCue = 3;
      break;
    case 55: //7
      if (selectedCue != 4) newCue = true;
      selectedCue = 4;
      break;
    case 56: //8
      if (selectedCue != 5) newCue = true;
      selectedCue = 5;
      break;
    case 57: //9
      if (selectedCue != 6) newCue = true;
      selectedCue = 6;
      break;
    case 32: //spacebar
      newShot = true;
      break;
    case 37: //left
      rotateCue = -1;
      break;
    case 39: //right
      rotateCue = 1;
      break;
  }
}

function onResize() {
  currentCamera.aspect = window.innerWidth / window.innerHeight;
  currentCamera.updateProjectionMatrix();
  renderer.setSize(window.innerWidth, window.innerHeight);
}


/*
    ANIMACAO
*/
function render() { renderer.render(scene, currentCamera); }

function animateCue(delta) {
  if (newCue) {
    for (let i = 1; i <= cuesPivots.length; i++)
      if (i != selectedCue) cuesPivots[i - 1].children[0].material = cueDefaultMaterial;
    cuesPivots[selectedCue - 1].children[0].material = cueSelectedMaterial;
    newCue = false;
  }
  if (rotateCue && selectedCue) {
    cuesPivots[selectedCue - 1].rotateY(rotateCue * delta);
    if (cuesPivots[selectedCue - 1].rotation.y < -CUE_MAX_ANGLE) {
      cuesPivots[selectedCue - 1].rotation.y = -CUE_MAX_ANGLE;
    } else if (cuesPivots[selectedCue - 1].rotation.y > CUE_MAX_ANGLE) {
      cuesPivots[selectedCue - 1].rotation.y = CUE_MAX_ANGLE;
    }
  }
}

function animateWhiteBall(delta) {
  if (rotateCue && selectedCue) {
    let ball = whiteBallsPivots[selectedCue - 1].children[0];
    ball.rotateY(rotateCue * delta);
    if (selectedCue == 2 || selectedCue == 3) {
      if (ball.rotation.y + Math.PI / 2 < -CUE_MAX_ANGLE) {
        ball.rotation.y = -CUE_MAX_ANGLE - Math.PI / 2;
      } else if (ball.rotation.y + Math.PI / 2 > CUE_MAX_ANGLE) {
        ball.rotation.y = CUE_MAX_ANGLE - Math.PI / 2;
      }
    }
    else if (selectedCue == 5 || selectedCue == 6) {
      if (ball.rotation.y - Math.PI / 2 < -CUE_MAX_ANGLE) {
        ball.rotation.y = -CUE_MAX_ANGLE + Math.PI / 2;
      } else if (ball.rotation.y - Math.PI / 2 > CUE_MAX_ANGLE) {
        ball.rotation.y = CUE_MAX_ANGLE + Math.PI / 2;
      }
    }
    else if (selectedCue == 1 || selectedCue == 4) {
      if (ball.rotation.y < -CUE_MAX_ANGLE) {
        ball.rotation.y = -CUE_MAX_ANGLE;
      } else if (ball.rotation.y > CUE_MAX_ANGLE) {
        ball.rotation.y = CUE_MAX_ANGLE;
      }
    }
  }
  rotateCue = 0;
}

function processShot() {
  if (newShot && selectedCue) {
    //make white ball collidable
    let ballPivot = whiteBallsPivots[selectedCue - 1];
    let newVelocity = new THREE.Vector3();
    (newVelocity.copy(ballPivot.velocity)).applyAxisAngle(new THREE.Vector3(0, 1, 0), cuesPivots[selectedCue - 1].rotation.y);
    (ballPivot.velocity).copy(newVelocity.multiplyScalar(SHOT_POWER));
    (ballPivot.children[0]).geometry.computeBoundingSphere();
    collisionBallsGeometryPivots.push(ballPivot);
    collisionBallsSpheres.push(new THREE.Sphere(ballPivot.position, (ballPivot.children[0]).geometry.boundingSphere.radius));

    //create ballCamera
    ballCamera = createBallCamera(ballPivot);
    ballPivot.add(ballCamera);
  }
  newShot = false;
}

function animateCollisionBalls(delta) {
  for (let i = 0; i < collisionBallsGeometryPivots.length; i++) {
    let ball = collisionBallsGeometryPivots[i];

    //make ball fall into hole
    if (ball.falling) {
      ball.position.y -= FALL_SPEED * delta;
      continue; //ignores rest of for content
    }

    //p = p0 + v0*t + 1/2*a*t^2
    //v = v0 + a*t

    //update ball position (+ v0*t)
    let displacement = new THREE.Vector3();
    (displacement.copy(ball.velocity)).multiplyScalar(delta);
    ball.position.add(displacement);

    //update ball velocity (+ 1/2*a*t^2)
    let acceleration = new THREE.Vector3();
    let half_a_t2 = new THREE.Vector3();
    let a_t = new THREE.Vector3();
    (acceleration.copy(ball.velocity)).negate();
    acceleration = (acceleration.normalize()).multiplyScalar(BALL_ACCELERATION);
    ball.position.add((half_a_t2.copy(acceleration)).multiplyScalar(1 / 2 * delta * delta))
    ball.velocity.add((a_t.copy(acceleration)).multiplyScalar(delta));

    //ball-wall collision
    if (ball.position.x + BALL_DIAMETER / 2 > TABLE_LENGTH / 2) {
      ball.position.x = TABLE_LENGTH / 2 - BALL_DIAMETER / 2;
      ball.velocity.x = -ball.velocity.x;
      alignInnerBallXWithVelocity(ball);
    }
    if (ball.position.x - BALL_DIAMETER / 2 < -TABLE_LENGTH / 2) {
      ball.position.x = -TABLE_LENGTH / 2 + BALL_DIAMETER / 2;
      ball.velocity.x = -ball.velocity.x;
      alignInnerBallXWithVelocity(ball);
    }
    if (ball.position.z + BALL_DIAMETER / 2 > TABLE_WIDTH / 2) {
      ball.position.z = TABLE_WIDTH / 2 - BALL_DIAMETER / 2;
      ball.velocity.z = -ball.velocity.z;
      alignInnerBallXWithVelocity(ball);
    }
    if (ball.position.z - BALL_DIAMETER / 2 < -TABLE_WIDTH / 2) {
      ball.position.z = -TABLE_WIDTH / 2 + BALL_DIAMETER / 2;
      ball.velocity.z = -ball.velocity.z;
      alignInnerBallXWithVelocity(ball);
    }

    //ball-ball collision
    collisionBallsSpheres[i].center.copy(ball.position); // update sphere position
    let o = hasCollisionOtherBalls(i);
    if (o != - 1) { //elastic collision
      let otherBall = collisionBallsGeometryPivots[o];

      let normal = new THREE.Vector3();
      normal.copy(ball.position).sub(otherBall.position).normalize();

      let relativeVelocity = new THREE.Vector3();
      relativeVelocity.copy(ball.velocity).sub(otherBall.velocity);

      let dot = relativeVelocity.dot(normal);

      normal = normal.multiplyScalar(dot);

      ball.velocity.sub(normal);
      alignInnerBallXWithVelocity(ball);
      otherBall.velocity.add(normal);

      ball.position = (ball.position.sub(displacement))/*.sub(acceleration)*/;
    }

    //bola a rodar sobre ela propria
    if (Math.abs(ball.velocity.x) > BALL_ACCELERATION / 20 || Math.abs(ball.velocity.z) > BALL_ACCELERATION / 20)
      ball.children[0].rotateZ(-ball.velocity.length() / 4 * delta);

  }
}

function animate() {
  let delta = clock.getDelta();
  animateCue(delta);
  animateWhiteBall(delta);
  processShot();
  checkIfBallsHole();
  animateCollisionBalls(delta);

  render();
  requestAnimationFrame(animate);
}


/*
  INICIALIZACAO
*/
function init() {
  renderer = new THREE.WebGLRenderer({ antialias: true });
  renderer.setSize(window.innerWidth, window.innerHeight);
  document.body.appendChild(renderer.domElement);

  createScene();

  currentCamera = topCamera = createTopCamera(0, 500, 0);       //x, y, z
  perspectiveCamera = createPerspectiveCamera(-200, 100, 200);  //x, y, z
  ballCamera = perspectiveCamera;

  render();

  window.addEventListener("resize", onResize);
  window.addEventListener("keydown", onKeyDown);
}