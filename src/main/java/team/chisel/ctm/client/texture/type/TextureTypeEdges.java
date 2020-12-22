package team.chisel.ctm.client.texture.type;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;

import team.chisel.ctm.api.texture.CTMTexture;
import team.chisel.ctm.api.util.TextureInfo;
import team.chisel.ctm.client.render.CTMLogic;
import team.chisel.ctm.client.texture.TextureEdges;
import team.chisel.ctm.client.texture.context.TextureContextCTM;
import team.chisel.ctm.client.util.ConnectionDirection;

public class TextureTypeEdges extends TextureTypeCTM {
	@Override
	public CTMTexture<? extends TextureTypeCTM> makeTexture(TextureInfo info) {
		return new TextureEdges(this, info);
	}

	@Override
	public TextureContextCTM getTextureContext(BlockState state, BlockView world, BlockPos pos, CTMTexture<?> tex) {
		return new TextureContextCTM(state, world, pos, (TextureEdges) tex) {
			@Override
			protected CTMLogic createCTM(BlockState state) {
				CTMLogic parent = super.createCTM(state);
				// FIXME
				CTMLogic ret = new CTMLogicEdges();
				ret.ignoreStates(parent.ignoreStates()).stateComparator(parent.stateComparator());
				ret.disableObscuredFaceCheck = parent.disableObscuredFaceCheck;
				return ret;
			}
		};
	}

	@Override
	public int requiredTextures() {
		return 3;
	}

	public static class CTMLogicEdges extends CTMLogic {
		private boolean obscured;

		public void setObscured(final boolean obscured) {
			this.obscured = obscured;
		}

		public boolean isObscured() {
			return this.obscured;
		}

		@Override
		public boolean isConnected(BlockView world, BlockPos current, BlockPos connection, Direction dir, BlockState state) {
			if (isObscured()) {
				return false;
			}
			BlockState obscuring = getConnectionState(world, current.offset(dir), dir, current);
			if (stateComparator(state, obscuring, dir)) {
				setObscured(true);
				return false;
			}
			BlockState con = getConnectionState(world, connection, dir, current);
			BlockState obscuringcon = getConnectionState(world, connection.offset(dir), dir, current);
			if (stateComparator(state, con, dir) || stateComparator(state, obscuringcon, dir)) {
				Vec3d difference = Vec3d.of(connection.subtract(current));
				if (difference.lengthSquared() > 1) {
					difference = difference.normalize();
					if (dir.getAxis() == Axis.Z) {
						difference = difference.rotateY((float) (-Math.PI / 2));
					}
					float ang = (float) Math.PI / 4;
					Vec3d vA;
					Vec3d vB;
					if (dir.getAxis().isVertical()) {
						vA = difference.rotateY(ang);
						vB = difference.rotateY(-ang);
					} else {
						vA = difference.rotateX(ang);
						vB = difference.rotateX(-ang);
					}
					BlockPos posA = new BlockPos(vA).add(current);
					BlockPos posB = new BlockPos(vB).add(current);
					return (getConnectionState(world, posA, dir, current) == state && !stateComparator(state, getConnectionState(world, posA.offset(dir), dir, current), dir)) || (getConnectionState(world, posB, dir, current) == state && !stateComparator(state, getConnectionState(world, posB.offset(dir), dir, current), dir));
				} else {
					return true;
				}
			}
			return false;
		}

		@Override
		protected void fillSubmaps(int idx) {
			ConnectionDirection[] dirs = SUBMAP_MAP[idx];
			if (!connectedOr(dirs[0], dirs[1]) && connected(dirs[2])) {
				submapCache[idx] = submapOffsets[idx];
			} else {
				super.fillSubmaps(idx);
			}
		}

		@Override
		public long serialized() {
			return isObscured() ? (super.serialized() | (1 << 8)) : super.serialized();
		}
	}
}
