const express = require("express");
const { vehiculos, llamarRpc } = require("./grpc-clients");

const router = express.Router();

router.get("/:patente", async (req, res, next) => {
  try {
    const vehiculo = await llamarRpc(
      vehiculos,
      "getVehiculoByPatente",
      { patente: req.params.patente }
    );

    res.json(vehiculo);
  } catch (error) {
    next(error);
  }
});

module.exports = router;