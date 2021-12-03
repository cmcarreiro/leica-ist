/*
    VARIAVEIS GLOBAIS
*/
var scene, renderer;
var currentCamera, perspectiveCamera, orthographicCamera;

var clock = new THREE.Clock();
var stage;
var cybertruck;
var floor;
var spotlights = [];
var directionalLight;

var shading = false;
var newMaterial = false;
var materialIndex = 0;
var cameraChanged = false;



/*
    OBJETOS
*/
function createDirectionalLight() {
  const LIGHT_COLOR = 0xFFFFFF;
  const LIGHT_INTENSITY = 0.5;

  let _directionalLight = new THREE.DirectionalLight(LIGHT_COLOR, LIGHT_INTENSITY);
  _directionalLight.on = true;
  return _directionalLight;
}


function createSpotlight(spotlightPosition) {

  function createCone(conePosition) {
    const CONE_RADIUS = 16;
    const CONE_HEIGHT = 30;
    const CONE_RADIALSEGMENTS = 32;
    const CONE_HEIGHTSEGMENTS = 32;

    let geometry = new THREE.ConeGeometry(CONE_RADIUS, CONE_HEIGHT, CONE_RADIALSEGMENTS, CONE_HEIGHTSEGMENTS, false);
    let material = new THREE.MeshPhongMaterial();
    let cone = new THREE.Mesh(geometry, material);
    cone.position.copy(conePosition);

    return cone;
  }

  function createSphere(spherePosition) {
    const SPHERE_RADIUS = 10;
    const SPHERE_RADIALSEGMENTS = 32;
    const SPHERE_HEIGHTSEGMENTS = 32;

    let geometry = new THREE.SphereGeometry(SPHERE_RADIUS, SPHERE_RADIALSEGMENTS, SPHERE_HEIGHTSEGMENTS);
    let material = new THREE.MeshPhongMaterial({ color: 0xFCCF05, emissive: 0xFCCF05, emissiveIntensity: 0 });
    let sphere = new THREE.Mesh(geometry, material);
    sphere.position.copy(spherePosition);

    return sphere;
  }

  function createLightSource(lightPosition) {
    let light = new THREE.SpotLight(0x0000FF);
    light.target = scene;
    light.position.copy(lightPosition);

    return light;
  }

  let _spotlight = new THREE.Object3D();
  _spotlight.position.copy(spotlightPosition);
  _spotlight.add(_spotlight.light = createLightSource(new THREE.Vector3(0, 0, 0)));
  _spotlight.add(_spotlight.cone = createCone(new THREE.Vector3(0, 0, 0)));
  _spotlight.add(_spotlight.sphere = createSphere(new THREE.Vector3(0, -10, 0)));

  _spotlight.on = true;
  _spotlight.lookAt(scene.position);
  _spotlight.rotateX(-90 * Math.PI / 180);
  return _spotlight;
}


function createFloor(floorPosition) {
  const SIDE = 1200;
  const SEGMENTS = 128;

  let geometry = new THREE.PlaneGeometry(SIDE, SIDE, SEGMENTS, SEGMENTS);
  let basicMaterial = new THREE.MeshBasicMaterial({ color: 0x70241f });
  let _floor = new THREE.Mesh(geometry, basicMaterial);

  _floor.materials = [];
  _floor.materials.push(basicMaterial);
  _floor.materials.push(new THREE.MeshLambertMaterial({ color: 0x70241f }));
  _floor.materials.push(new THREE.MeshPhongMaterial({ color: 0x70241f }));

  _floor.rotateX(-90 * Math.PI / 180);
  _floor.position.copy(floorPosition);

  return _floor;
}


function createStage(stagePosition) {
  const DIAMETER = 800;
  const RADIUS_TOP = DIAMETER / 2;
  const RADIUS_BOTTOM = DIAMETER / 2;
  const HEIGHT = 50;
  const SEGMENTS = 128;
  const ANGULAR_VELOCITY = 120 * Math.PI / 180;

  let geometry = new THREE.CylinderGeometry(RADIUS_TOP, RADIUS_BOTTOM, HEIGHT, SEGMENTS, SEGMENTS);
  let basicMaterial = new THREE.MeshBasicMaterial({ color: 0xFFFFFF });
  let _stage = new THREE.Mesh(geometry, basicMaterial);

  _stage.materials = [];
  _stage.materials.push(basicMaterial);
  _stage.materials.push(new THREE.MeshLambertMaterial({ color: 0xFFFFFF }));
  _stage.materials.push(new THREE.MeshPhongMaterial({ color: 0xFFFFFF }));

  _stage.angularVelocity = ANGULAR_VELOCITY;
  _stage.direction = 0;

  _stage.position.copy(stagePosition);
  return _stage;
}


function createCybertruck(cybertruckPosition) {

  function createFrame(framePosition) {
    const WHEEL_DISTANCE_Z = 172;
    const WHEEL_HALFDISTANCE_Z = WHEEL_DISTANCE_Z / 2;
    const WHEEL_DISTANCE_X = 380;
    const WHEEL_HALFDISTANCE_X = WHEEL_DISTANCE_X / 2;

    const WHEEL_RADIUS = 95 / 2;
    const WHEEL_THICKNESS = 30;
    const AXIS_DIAMETER = 10;
    const AXIS_RADIUS = AXIS_DIAMETER / 2;

    function createWheel(wheelPosition) {
      const WHEEL_RADIALSEGMENTS = 64;

      let geometry = new THREE.CylinderGeometry(WHEEL_RADIUS, WHEEL_RADIUS, WHEEL_THICKNESS, WHEEL_RADIALSEGMENTS);
      let basicMaterial = new THREE.MeshBasicMaterial({ color: 0x2D3033 });
      let wheel = new THREE.Mesh(geometry, basicMaterial);

      wheel.materials = [];
      wheel.materials.push(basicMaterial);
      wheel.materials.push(new THREE.MeshLambertMaterial({ color: 0x2D3033 }));
      wheel.materials.push(new THREE.MeshPhongMaterial({ color: 0x2D3033 }));


      wheel.rotateX(90 * Math.PI / 180);
      wheel.position.copy(wheelPosition);
      return wheel;
    }

    function createAxis(axisPosition) {
      const AXIS_RADIALSEGMENTS = 64;

      let geometry = new THREE.CylinderGeometry(AXIS_RADIUS, AXIS_RADIUS, WHEEL_DISTANCE_Z, AXIS_RADIALSEGMENTS);
      let basicMaterial = new THREE.MeshBasicMaterial({ color: 0xFFFFFF });
      let axis = new THREE.Mesh(geometry, basicMaterial);

      axis.materials = [];
      axis.materials.push(basicMaterial);
      axis.materials.push(new THREE.MeshLambertMaterial({ color: 0xFFFFFF }));
      axis.materials.push(new THREE.MeshPhongMaterial({ color: 0xFFFFFF }));

      axis.rotateX(90 * Math.PI / 180);
      axis.position.copy(axisPosition);

      return axis;
    }

    function createBoard(boardPosition) {

      let geometry = new THREE.BoxGeometry(WHEEL_DISTANCE_X, AXIS_DIAMETER, WHEEL_DISTANCE_Z - WHEEL_THICKNESS);
      let basicMaterial = new THREE.MeshBasicMaterial({ color: 0xFFFFFF });
      let board = new THREE.Mesh(geometry, basicMaterial);

      board.materials = [];
      board.materials.push(basicMaterial);
      board.materials.push(new THREE.MeshLambertMaterial({ color: 0xFFFFFF }));
      board.materials.push(new THREE.MeshPhongMaterial({ color: 0xFFFFFF }));

      board.position.copy(boardPosition);

      return board;
    }

    let frame = new THREE.Object3D();
    frame.add(createWheel(new THREE.Vector3(-WHEEL_HALFDISTANCE_X, WHEEL_RADIUS, WHEEL_HALFDISTANCE_Z)));
    frame.add(createWheel(new THREE.Vector3(-WHEEL_HALFDISTANCE_X, WHEEL_RADIUS, -WHEEL_HALFDISTANCE_Z)));
    frame.add(createWheel(new THREE.Vector3(WHEEL_HALFDISTANCE_X, WHEEL_RADIUS, WHEEL_HALFDISTANCE_Z)));
    frame.add(createWheel(new THREE.Vector3(WHEEL_HALFDISTANCE_X, WHEEL_RADIUS, -WHEEL_HALFDISTANCE_Z)));
    frame.add(createAxis(new THREE.Vector3(-WHEEL_HALFDISTANCE_X, WHEEL_RADIUS, 0)))
    frame.add(createAxis(new THREE.Vector3(WHEEL_HALFDISTANCE_X, WHEEL_RADIUS, 0)))
    frame.add(frame.board = createBoard(new THREE.Vector3(0, WHEEL_RADIUS, 0)));

    frame.position.copy(framePosition);
    return frame;
  }

  function addFace(a, b, c, geometry) {
    geometry.vertices.push(a, b, c);
    let len = geometry.vertices.length;
    geometry.faces.push(new THREE.Face3(len - 3, len - 2, len - 1));
  }

  function createFrontWindow() {
    let geometry = new THREE.Geometry();
    let basicMaterial = new THREE.MeshBasicMaterial({ color: 0x59CBE8 });

    addFace(
      new THREE.Vector3(40, 183, 61),
      new THREE.Vector3(188, 137, 77),
      new THREE.Vector3(188, 137, -77),
      geometry);
    addFace(
      new THREE.Vector3(40, 183, 61),
      new THREE.Vector3(188, 137, -77),
      new THREE.Vector3(40, 183, -61),
      geometry);

    geometry.computeFaceNormals();
    geometry.computeVertexNormals();

    let frontWindow = new THREE.Mesh(geometry, basicMaterial);
    frontWindow.materials = [];
    frontWindow.materials.push(basicMaterial);
    frontWindow.materials.push(new THREE.MeshLambertMaterial({ color: 0x59CBE8 }));
    frontWindow.materials.push(new THREE.MeshPhongMaterial({ color: 0x59CBE8, specular: 0xFFFFFF, shininess: 100 }));

    return frontWindow;
  }

  function createLeftWindow() {
    let geometry = new THREE.Geometry();
    let basicMaterial = new THREE.MeshBasicMaterial({ color: 0x59CBE8 });

    addFace(
      new THREE.Vector3(27, 185, -67),
      new THREE.Vector3(194, 130, -84),
      new THREE.Vector3(-105, 147, -84),
      geometry);
    addFace(
      new THREE.Vector3(27, 185, -67),
      new THREE.Vector3(-105, 147, -84),
      new THREE.Vector3(-100, 167, -76),
      geometry);

    geometry.computeFaceNormals();
    geometry.computeVertexNormals();

    let leftWindow = new THREE.Mesh(geometry, basicMaterial);
    leftWindow.materials = [];
    leftWindow.materials.push(basicMaterial);
    leftWindow.materials.push(new THREE.MeshLambertMaterial({ color: 0x59CBE8 }));
    leftWindow.materials.push(new THREE.MeshPhongMaterial({ color: 0x59CBE8, specular: 0x59CBE8, shininess: 100 }));

    return leftWindow;
  }

  function createRightWindow() {
    let geometry = new THREE.Geometry();
    let basicMaterial = new THREE.MeshBasicMaterial({ color: 0x59CBE8 });

    addFace(
      new THREE.Vector3(27, 185, 67),
      new THREE.Vector3(-105, 147, 84),
      new THREE.Vector3(194, 130, 84),
      geometry);
    addFace(
      new THREE.Vector3(27, 185, 67),
      new THREE.Vector3(-100, 167, 76),
      new THREE.Vector3(-105, 147, 84),
      geometry);

    geometry.computeFaceNormals();
    geometry.computeVertexNormals();

    let rightWindow = new THREE.Mesh(geometry, basicMaterial);
    rightWindow.materials = [];
    rightWindow.materials.push(basicMaterial);
    rightWindow.materials.push(new THREE.MeshLambertMaterial({ color: 0x59CBE8 }));
    rightWindow.materials.push(new THREE.MeshPhongMaterial({ color: 0x59CBE8, specular: 0x59CBE8, shininess: 100 }));

    return rightWindow;
  }

  function createFrontLights() {
    let geometry = new THREE.Geometry();
    let basicMaterial = new THREE.MeshBasicMaterial({ color: 0xFFFF00 });

    addFace(
      new THREE.Vector3(251, 117, 88),
      new THREE.Vector3(251, 107, 88),
      new THREE.Vector3(277, 105, 64),
      geometry);
    addFace(
      new THREE.Vector3(251, 117, 88),
      new THREE.Vector3(277, 105, 64),
      new THREE.Vector3(277, 110, 64),
      geometry);

    addFace(
      new THREE.Vector3(277, 110, 64),
      new THREE.Vector3(277, 105, 64),
      new THREE.Vector3(277, 105, -64),
      geometry);
    addFace(
      new THREE.Vector3(277, 110, 64),
      new THREE.Vector3(277, 105, -64),
      new THREE.Vector3(277, 110, -64),
      geometry);

    addFace(
      new THREE.Vector3(277, 105, -64),
      new THREE.Vector3(251, 107, -88),
      new THREE.Vector3(277, 110, -64),
      geometry);
    addFace(
      new THREE.Vector3(277, 110, -64),
      new THREE.Vector3(251, 107, -88),
      new THREE.Vector3(251, 117, -88),
      geometry);

    geometry.computeFaceNormals();
    geometry.computeVertexNormals();

    let frontLights = new THREE.Mesh(geometry, basicMaterial);
    frontLights.materials = [];
    frontLights.materials.push(basicMaterial);
    frontLights.materials.push(new THREE.MeshLambertMaterial({ color: 0xFFFF00, emissive: 0xFFFF00 }));
    frontLights.materials.push(new THREE.MeshPhongMaterial({ color: 0xFFFF00, emissive: 0xFFFF00, specular: 0xFFFF00, shininess: 100 }));

    return frontLights;
  }

  function createRearLights() {
    let geometry = new THREE.Geometry();
    let basicMaterial = new THREE.MeshBasicMaterial({ color: 0xFF0000 });

    addFace(
      new THREE.Vector3(-304, 147, -88),
      new THREE.Vector3(-304, 137, -88),
      new THREE.Vector3(-304, 137, 88),
      geometry);
    addFace(
      new THREE.Vector3(-304, 147, -88),
      new THREE.Vector3(-304, 137, 88),
      new THREE.Vector3(-304, 147, 88),
      geometry);

    geometry.computeFaceNormals();
    geometry.computeVertexNormals();

    let rearLights = new THREE.Mesh(geometry, basicMaterial);
    rearLights.materials = [];
    rearLights.materials.push(basicMaterial);
    rearLights.materials.push(new THREE.MeshLambertMaterial({ color: 0xFF0000, emissive: 0xFF0000 }));
    rearLights.materials.push(new THREE.MeshPhongMaterial({ color: 0xFF0000, emissive: 0xFF0000, specular: 0xFFFF00, shininess: 100 }));

    return rearLights;
  }

  function createFrontBody() {
    let geometry = new THREE.Geometry();
    let basicMaterial = new THREE.MeshBasicMaterial({ color: 0xA8A9AD });

    addFace(
      new THREE.Vector3(251, 107, 88),
      new THREE.Vector3(242, 50, 88),
      new THREE.Vector3(277, 50, 64),
      geometry);
    addFace(
      new THREE.Vector3(277, 50, 64),
      new THREE.Vector3(277, 105, 64),
      new THREE.Vector3(251, 107, 88),
      geometry);

    addFace(
      new THREE.Vector3(277, 105, 64),
      new THREE.Vector3(277, 50, 64),
      new THREE.Vector3(277, 50, -64),
      geometry);
    addFace(
      new THREE.Vector3(277, 50, -64),
      new THREE.Vector3(277, 105, -64),
      new THREE.Vector3(277, 105, 64),
      geometry);

    addFace(
      new THREE.Vector3(277, 105, -64),
      new THREE.Vector3(277, 50, -64),
      new THREE.Vector3(242, 50, -88),
      geometry);
    addFace(
      new THREE.Vector3(242, 50, -88),
      new THREE.Vector3(251, 107, -88),
      new THREE.Vector3(277, 105, -64),
      geometry);

    addFace(
      new THREE.Vector3(195, 133, 85),
      new THREE.Vector3(251, 117, 88),
      new THREE.Vector3(277, 110, 64),
      geometry);
    addFace(
      new THREE.Vector3(277, 110, 64),
      new THREE.Vector3(277, 110, -64),
      new THREE.Vector3(195, 133, 85),
      geometry);
    addFace(
      new THREE.Vector3(277, 110, -64),
      new THREE.Vector3(195, 133, -85),
      new THREE.Vector3(195, 133, 85),
      geometry);
    addFace(
      new THREE.Vector3(277, 110, -64),
      new THREE.Vector3(251, 117, -88),
      new THREE.Vector3(195, 133, -85),
      geometry);

    addFace(
      new THREE.Vector3(28, 190, 65),
      new THREE.Vector3(195, 133, 85),
      new THREE.Vector3(188, 137, 77),
      geometry);
    addFace(
      new THREE.Vector3(188, 137, 77),
      new THREE.Vector3(40, 183, 61),
      new THREE.Vector3(28, 190, 65),
      geometry);

    addFace(
      new THREE.Vector3(188, 137, 77),
      new THREE.Vector3(195, 133, 85),
      new THREE.Vector3(195, 133, -85),
      geometry);
    addFace(
      new THREE.Vector3(195, 133, -85),
      new THREE.Vector3(188, 137, -77),
      new THREE.Vector3(188, 137, 77),
      geometry);

    addFace(
      new THREE.Vector3(195, 133, -85),
      new THREE.Vector3(188, 137, -77),
      new THREE.Vector3(188, 137, 77),
      geometry);

    addFace(
      new THREE.Vector3(188, 137, -77),
      new THREE.Vector3(195, 133, -85),
      new THREE.Vector3(28, 190, -65),
      geometry);

    addFace(
      new THREE.Vector3(188, 137, -77),
      new THREE.Vector3(28, 190, -65),
      new THREE.Vector3(40, 183, -61),
      geometry);

    addFace(
      new THREE.Vector3(28, 190, 65),
      new THREE.Vector3(40, 183, 61),
      new THREE.Vector3(40, 183, -61),
      geometry);

    addFace(
      new THREE.Vector3(40, 183, -61),
      new THREE.Vector3(28, 190, -65),
      new THREE.Vector3(28, 190, 65),
      geometry);

    geometry.computeFaceNormals();
    geometry.computeVertexNormals();

    let frontBody = new THREE.Mesh(geometry, basicMaterial);
    frontBody.materials = [];
    frontBody.materials.push(basicMaterial);
    frontBody.materials.push(new THREE.MeshLambertMaterial({ color: 0xA8A9AD }));
    frontBody.materials.push(new THREE.MeshPhongMaterial({ color: 0xA8A9AD, specular: 0xA8A9AD, shininess: 60 }));

    return frontBody;
  }

  function createRearBody() {
    let geometry = new THREE.Geometry();
    let basicMaterial = new THREE.MeshBasicMaterial({ color: 0xA8A9AD });

    addFace(
      new THREE.Vector3(28, 190, -65),
      new THREE.Vector3(-304, 147, -88),
      new THREE.Vector3(-304, 147, 88),
      geometry);
    addFace(
      new THREE.Vector3(-304, 147, 88),
      new THREE.Vector3(28, 190, 65),
      new THREE.Vector3(28, 190, -65),
      geometry);

    addFace(
      new THREE.Vector3(-304, 137, -88),
      new THREE.Vector3(-292, 44, -80),
      new THREE.Vector3(-292, 44, 80),
      geometry);
    addFace(
      new THREE.Vector3(-292, 44, 80),
      new THREE.Vector3(-304, 137, 88),
      new THREE.Vector3(-304, 137, -88),
      geometry);

    geometry.computeFaceNormals();
    geometry.computeVertexNormals();

    let rearBody = new THREE.Mesh(geometry, basicMaterial);
    rearBody.materials = [];
    rearBody.materials.push(basicMaterial);
    rearBody.materials.push(new THREE.MeshLambertMaterial({ color: 0xA8A9AD }));
    rearBody.materials.push(new THREE.MeshPhongMaterial({ color: 0xA8A9AD, specular: 0xA8A9AD, shininess: 60 }));

    return rearBody;
  }

  function createRightBody() {
    let geometry = new THREE.Geometry();
    let basicMaterial = new THREE.MeshBasicMaterial({ color: 0xA8A9AD });

    addFace(
      new THREE.Vector3(251, 117, 88),
      new THREE.Vector3(222, 96, 88),
      new THREE.Vector3(251, 107, 88),
      geometry);
    addFace(
      new THREE.Vector3(251, 107, 88),
      new THREE.Vector3(222, 96, 88),
      new THREE.Vector3(242, 50, 88),
      geometry);
    addFace(
      new THREE.Vector3(251, 117, 88),
      new THREE.Vector3(158, 96, 88),
      new THREE.Vector3(222, 96, 88),
      geometry);
    addFace(
      new THREE.Vector3(251, 117, 88),
      new THREE.Vector3(-304, 147, 88),
      new THREE.Vector3(158, 96, 88),
      geometry);
    addFace(
      new THREE.Vector3(158, 96, 88),
      new THREE.Vector3(-304, 147, 88),
      new THREE.Vector3(-160, 97, 88),
      geometry);
    addFace(
      new THREE.Vector3(158, 96, 88),
      new THREE.Vector3(-304, 147, 88),
      new THREE.Vector3(-160, 97, 88),
      geometry);
    addFace(
      new THREE.Vector3(158, 96, 88),
      new THREE.Vector3(-160, 97, 88),
      new THREE.Vector3(135, 36, 88),
      geometry);
    addFace(
      new THREE.Vector3(-160, 97, 88),
      new THREE.Vector3(-133, 36, 88),
      new THREE.Vector3(135, 36, 88),
      geometry);
    addFace(
      new THREE.Vector3(-304, 147, 88),
      new THREE.Vector3(-223, 97, 88),
      new THREE.Vector3(-160, 97, 88),
      geometry);
    addFace(
      new THREE.Vector3(-304, 147, 88),
      new THREE.Vector3(-244, 43, 88),
      new THREE.Vector3(-223, 97, 88),
      geometry);
    addFace(
      new THREE.Vector3(-304, 147, 88),
      new THREE.Vector3(-304, 137, 88),
      new THREE.Vector3(-244, 43, 88),
      geometry);
    addFace(
      new THREE.Vector3(-304, 137, 88),
      new THREE.Vector3(-292, 44, 80),
      new THREE.Vector3(-244, 43, 88),
      geometry);

    addFace(
      new THREE.Vector3(28, 190, 65),
      new THREE.Vector3(194, 130, 84),
      new THREE.Vector3(195, 133, 85),
      geometry);

    addFace(
      new THREE.Vector3(195, 133, 85),
      new THREE.Vector3(194, 130, 84),
      new THREE.Vector3(251, 117, 88),
      geometry);


    addFace(
      new THREE.Vector3(28, 190, 65),
      new THREE.Vector3(27, 185, 67),
      new THREE.Vector3(194, 130, 84),
      geometry);
    addFace(
      new THREE.Vector3(28, 190, 65),
      new THREE.Vector3(-100, 167, 76),
      new THREE.Vector3(27, 185, 67),
      geometry);
    addFace(
      new THREE.Vector3(28, 190, 65),
      new THREE.Vector3(-304, 147, 88),
      new THREE.Vector3(-100, 167, 76),
      geometry);
    addFace(
      new THREE.Vector3(-100, 167, 76),
      new THREE.Vector3(-304, 147, 88),
      new THREE.Vector3(-105, 147, 84),
      geometry);
    addFace(
      new THREE.Vector3(-105, 147, 84),
      new THREE.Vector3(-304, 147, 88),
      new THREE.Vector3(251, 117, 88),
      geometry);
    addFace(
      new THREE.Vector3(194, 130, 84),
      new THREE.Vector3(-105, 147, 84),
      new THREE.Vector3(251, 117, 88),
      geometry);

    geometry.computeFaceNormals();
    geometry.computeVertexNormals();

    let rightBody = new THREE.Mesh(geometry, basicMaterial);
    rightBody.materials = [];
    rightBody.materials.push(basicMaterial);
    rightBody.materials.push(new THREE.MeshLambertMaterial({ color: 0xA8A9AD }));
    rightBody.materials.push(new THREE.MeshPhongMaterial({ color: 0xA8A9AD, specular: 0xA8A9AD, shininess: 60 }));
    return rightBody;
  }

  function createLeftBody() {
    let geometry = new THREE.Geometry();
    let basicMaterial = new THREE.MeshBasicMaterial({ color: 0xA8A9AD });

    addFace(
      new THREE.Vector3(251, 117, -88),
      new THREE.Vector3(251, 107, -88),
      new THREE.Vector3(222, 96, -88),
      geometry);
    addFace(
      new THREE.Vector3(251, 107, -88),
      new THREE.Vector3(242, 50, -88),
      new THREE.Vector3(222, 96, -88),
      geometry);
    addFace(
      new THREE.Vector3(251, 117, -88),
      new THREE.Vector3(222, 96, -88),
      new THREE.Vector3(158, 96, -88),
      geometry);
    addFace(
      new THREE.Vector3(251, 117, -88),
      new THREE.Vector3(158, 96, -88),
      new THREE.Vector3(-304, 147, -88),
      geometry);
    addFace(
      new THREE.Vector3(158, 96, -88),
      new THREE.Vector3(-160, 97, -88),
      new THREE.Vector3(-304, 147, -88),
      geometry);
    addFace(
      new THREE.Vector3(158, 96, -88),
      new THREE.Vector3(-160, 97, -88),
      new THREE.Vector3(-304, 147, -88),
      geometry);
    addFace(
      new THREE.Vector3(158, 96, -88),
      new THREE.Vector3(135, 36, -88),
      new THREE.Vector3(-160, 97, -88),
      geometry);
    addFace(
      new THREE.Vector3(-160, 97, -88),
      new THREE.Vector3(135, 36, -88),
      new THREE.Vector3(-133, 36, -88),
      geometry);
    addFace(
      new THREE.Vector3(-304, 147, -88),
      new THREE.Vector3(-160, 97, -88),
      new THREE.Vector3(-223, 97, -88),
      geometry);
    addFace(
      new THREE.Vector3(-304, 147, -88),
      new THREE.Vector3(-223, 97, -88),
      new THREE.Vector3(-244, 43, -88),
      geometry);
    addFace(
      new THREE.Vector3(-304, 147, -88),
      new THREE.Vector3(-244, 43, -88),
      new THREE.Vector3(-304, 137, -88),
      geometry);
    addFace(
      new THREE.Vector3(-304, 137, -88),
      new THREE.Vector3(-244, 43, -88),
      new THREE.Vector3(-292, 44, -80),
      geometry);

    addFace(
      new THREE.Vector3(28, 190, -65),
      new THREE.Vector3(195, 133, -85),
      new THREE.Vector3(194, 130, -84),
      geometry);

    addFace(
      new THREE.Vector3(195, 133, -85),
      new THREE.Vector3(251, 117, -88),
      new THREE.Vector3(194, 130, -84),
      geometry);

    addFace(
      new THREE.Vector3(28, 190, -65),
      new THREE.Vector3(194, 130, -84),
      new THREE.Vector3(27, 185, -67),
      geometry);
    addFace(
      new THREE.Vector3(28, 190, -65),
      new THREE.Vector3(27, 185, -67),
      new THREE.Vector3(-100, 167, -76),
      geometry);
    addFace(
      new THREE.Vector3(28, 190, -65),
      new THREE.Vector3(-100, 167, -76),
      new THREE.Vector3(-304, 147, -88),
      geometry);
    addFace(
      new THREE.Vector3(-100, 167, -76),
      new THREE.Vector3(-105, 147, -84),
      new THREE.Vector3(-304, 147, -88),
      geometry);
    addFace(
      new THREE.Vector3(-105, 147, -84),
      new THREE.Vector3(251, 117, -88),
      new THREE.Vector3(-304, 147, -88),
      geometry);
    addFace(
      new THREE.Vector3(194, 130, -84),
      new THREE.Vector3(251, 117, -88),
      new THREE.Vector3(-105, 147, -84),
      geometry);

    geometry.computeFaceNormals();
    geometry.computeVertexNormals();

    let leftBody = new THREE.Mesh(geometry, basicMaterial);
    leftBody.materials = [];
    leftBody.materials.push(basicMaterial);
    leftBody.materials.push(new THREE.MeshLambertMaterial({ color: 0xA8A9AD }));
    leftBody.materials.push(new THREE.MeshPhongMaterial({ color: 0xA8A9AD, specular: 0xA8A9AD, shininess: 60 }));
    return leftBody;
  }

  let _cybertruck = new THREE.Object3D();
  _cybertruck.add(_cybertruck.frame = createFrame(new THREE.Vector3(0, 0, 0)))
  _cybertruck.add(_cybertruck.frontWindow = createFrontWindow());
  _cybertruck.add(_cybertruck.leftWindow = createLeftWindow());
  _cybertruck.add(_cybertruck.rightWindow = createRightWindow());
  _cybertruck.add(_cybertruck.createFrontLights = createFrontLights());
  _cybertruck.add(_cybertruck.createRearLights = createRearLights());
  _cybertruck.add(_cybertruck.frontBody = createFrontBody());
  _cybertruck.add(_cybertruck.rearBody = createRearBody());
  _cybertruck.add(_cybertruck.leftBody = createLeftBody());
  _cybertruck.add(_cybertruck.rightBody = createRightBody());
  _cybertruck.position.copy(cybertruckPosition);
  return _cybertruck;
}



/*
    CENA E CAMARAS
*/
function createScene() {
  scene = new THREE.Scene();
  scene.background = new THREE.Color(0x000000);

  spotlights.push(createSpotlight(new THREE.Vector3(500, 250, 0)));
  spotlights.push(createSpotlight(new THREE.Vector3(-350, 250, -350)));
  spotlights.push(createSpotlight(new THREE.Vector3(-350, 250, 350)));
  floor = createFloor(new THREE.Vector3(0, -50, 0));
  stage = createStage(new THREE.Vector3(0, -25, 0));
  cybertruck = createCybertruck(new THREE.Vector3(0, 25, 0));
  stage.add(cybertruck);
  directionalLight = createDirectionalLight();

  scene.add(new THREE.AxesHelper(2000));
  for (let i = 0; i < spotlights.length; i++)
    scene.add(spotlights[i]);

  scene.add(floor);
  scene.add(stage); // and cybertruck
  scene.add(directionalLight);
}


function createOrthographicCamera(orthographicCameraPosition) {
  const aspect = window.innerWidth / window.innerHeight;
  const frustumSize = 600;
  let left = frustumSize * aspect / -2;
  let right = frustumSize * aspect / 2;
  let top = frustumSize / 2;
  let bottom = frustumSize / -2;
  let near = 1;
  let far = 2000;
  let camera = new THREE.OrthographicCamera(left, right, top, bottom, near, far);
  camera.frustumSize = frustumSize;

  camera.position.copy(orthographicCameraPosition);

  camera.lookAt(new THREE.Vector3(0, orthographicCameraPosition.y, 0));
  stage.add(camera);
  return camera;
}


function createPerspectiveCamera(perspectiveCameraPosition) {
  let fov = 50;
  let aspect = window.innerWidth / window.innerHeight;
  let near = 1;
  let far = 2000;

  let camera = new THREE.PerspectiveCamera(fov, aspect, near, far);
  camera.position.copy(perspectiveCameraPosition);
  camera.lookAt(scene.position);
  return camera;
}



/*
    EVENTOS
*/
function onKeyDown(e) {
  switch (e.keyCode) {
    case 49: //1
      spotlights[0].on = !spotlights[0].on;
      break;
    case 50: //2
      spotlights[1].on = !spotlights[1].on;
      break;
    case 51: //3
      spotlights[2].on = !spotlights[2].on;
      break;
    case 52: //4
      currentCamera = perspectiveCamera;
      cameraChanged = true;
      break;
    case 53: //5
      currentCamera = orthographicCamera;
      cameraChanged = true;
      break;
    case 81: //q
      directionalLight.on = !directionalLight.on;
      break;
    case 87: //w
      shading = !shading;
      newMaterial = true;
      break;
    case 69: //e
      materialIndex = (materialIndex + 1) % 2
      newMaterial = true;
      break;
    case 37: //left
      stage.direction = -1;
      break;
    case 39: //right
      stage.direction = 1;
      break;

  }
}

function onWindowResize() {
  let aspect = window.innerWidth / window.innerHeight;

  if (currentCamera == perspectiveCamera) {
    perspectiveCamera.aspect = aspect;
    perspectiveCamera.updateProjectionMatrix();
    currentCamera = perspectiveCamera;
  }

  else if (currentCamera == orthographicCamera) {
    orthographicCamera.left = orthographicCamera.frustumSize * aspect / - 2;
    orthographicCamera.right = orthographicCamera.frustumSize * aspect / 2;
    orthographicCamera.top = orthographicCamera.frustumSize / 2;
    orthographicCamera.bottom = - orthographicCamera.frustumSize / 2;
    orthographicCamera.updateProjectionMatrix();
    currentCamera = orthographicCamera;
  }
  renderer.setSize(window.innerWidth, window.innerHeight);
}

/*
    ANIMACAO
*/
function updateLights() {
  for (let i = 0; i < spotlights.length; i++) {
    spotlights[i].light.visible = spotlights[i].on ? true : false;
    spotlights[i].sphere.material.emissiveIntensity = spotlights[i].on ? 1 : 0;
  }

  directionalLight.visible = directionalLight.on ? true : false;
}


function updateMaterials() {
  if (newMaterial) {
    let index = shading ? materialIndex + 1 : 0

    //floor
    floor.material = floor.materials[index];
    //stage
    stage.material = stage.materials[index];
    // cybertruck
    for (let i = 0; i < cybertruck.children.length; i++) {
      if (cybertruck.children[i] == cybertruck.frame) {
        for (let j = 0; j < cybertruck.frame.children.length; j++)
          cybertruck.frame.children[j].material = cybertruck.frame.children[j].materials[index];
      }
      else
        cybertruck.children[i].material = cybertruck.children[i].materials[index];
    }
    newMaterial = false;
  }
}


function updateStage(delta) {
  stage.rotateY(stage.angularVelocity * stage.direction * delta);
  stage.direction = 0;
}

function updateCamera() {
  if (cameraChanged) {
    onWindowResize();
    cameraChanged = false;
  }
}


function render() {
  renderer.render(scene, currentCamera);
}


function animate() {
  let delta = clock.getDelta();
  updateLights();
  updateMaterials();
  updateStage(delta);
  updateCamera();

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
  currentCamera = perspectiveCamera = createPerspectiveCamera(new THREE.Vector3(500, 500, 500));    //x, y, z
  orthographicCamera = createOrthographicCamera(new THREE.Vector3(0, 200, 600));       //x, y, z

  onWindowResize();

  window.addEventListener("keydown", onKeyDown);
  window.addEventListener("resize", onWindowResize);

  render();
}
