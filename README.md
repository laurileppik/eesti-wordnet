# Eesti Wordnet

Eesti Wordneti veebirakendus, mis võimaldab otsida ja sirvida Eesti Wordneti andmeid.

---

## Installeerimisjuhend (lokaalne arenduskeskkond)

### Eeltingimused

- **Linux** (nt Ubuntu 24.10). Võimalused:
  - [VirtualBox + Ubuntu](https://courses.cs.ut.ee/2025/os/fall/Main/Praktikum3)
  - WSL (Windows Subsystem for Linux)
- **Docker** – paigaldusjuhend Ubuntu jaoks: https://docs.docker.com/engine/install/ubuntu/

---

### Paigaldamine

Ava terminal ja jooksuta järgmised käsud:

```bash
sudo -i

sudo apt update

sudo apt install git-lfs

git lfs install

git clone https://github.com/laurileppik/eesti-wordnet

cd eesti-wordnet

chmod +x run-dev.sh

chmod +x wait-for-db-and-generate.sh

./run-dev.sh
```

---

### Käivitamine

Skripti `./run-dev.sh` esmane jooksutamine võib võtta **kuni ~20 minutit**, kuna:
- ehitatakse Dockeri konteinerid
- lisatakse andmed lokaalsesse andmebaasi

Kui rakendus on edukalt käivitunud, on see kättesaadav aadressil **http://localhost**.

Edukat käivitumist saab kontrollida nii: otsingukasti teksti sisestades hakkab rakendus sõnu soovitama.

---

### Dockeri konteinerite puhastamine

Kõikide konteinerite, piltide ja ehitamise vahemälu eemaldamiseks:

```bash
# Peata kõik konteinerid
docker stop $(docker ps -aq)

# Eemalda kõik konteinerid
docker rm $(docker ps -aq)

# Eemalda kõik pildid
docker rmi -f $(docker images -aq)

# Eemalda kõik võrgud
docker network prune -f

# Eemalda ehitamise protsessi vahemälu
docker builder prune -af

# Eemalda kõik kasutamata andmed
docker system prune -af --volumes
```

---

## Tootmiskeskkond (ingl Production)

Tootmiskeskkond kasutab `docker-compose.prod.yml` faili ning ühendub välise PostgreSQL andmebaasiga. Lokaalset mock-andmebaasi ei kasutata.

### Eeltingimused

- Linux server (nt Ubuntu 24.10)
- Docker paigaldatud (vt https://docs.docker.com/engine/install/ubuntu/)
- Ligipääs välisele andmebaasile

### Keskkonna muutujad

Loo projekti juurkausta `.env` fail järgmise sisuga ja lisa tegelikud väärtused:

```dotenv
DB_URL=sinu_andmebaasi_url
DB_USER=sinu_kasutajanimi
DB_PASSWORD=sinu_parool
```

### Käivitamine

```bash
sudo -i

sudo apt update

sudo apt install git-lfs

git lfs install

git clone https://github.com/laurileppik/eesti-wordnet

cd eesti-wordnet

# Seadista .env fail (vt eespool)

docker compose -f docker-compose.prod.yml up --build -d
```

Rakendus käivitub:
- **Frontend:** http://localhost (port 80)
- **Backend:** http://localhost:8080 (port 8080 → konteineris 8083)

### Rakenduse peatamine

```bash
docker compose -f docker-compose.prod.yml down
```

### Rakenduse uuendamine

```bash
git pull

docker compose -f docker-compose.prod.yml up --build -d
```

